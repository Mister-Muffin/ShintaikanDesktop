package dialogs

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import getTotalTrainingSessions
import model.Member
import model.Participation
import next
import nextStickerUnit
import stickerUnits
import java.time.LocalDate

@Composable
fun StickerDialog(
    stickerStudentsList: List<Member>,
    participations: List<Participation>,
    updateSticker: (Member, Int, String) -> Unit,
    activeTrainer: Member,
    onDismiss: () -> Unit
) {
    val mutableMembers = remember { stickerStudentsList.toMutableStateList() }
    fun SnapshotStateList<Member>.filterShowSticker() = filter { it.stickerShowAgain }

    val lazyState = rememberLazyListState()

    /**
     * This function returns true if all radio buttons have been clicked at lease once to ensure,
     * that the user has made his desicion for each student
     */
    fun buttonEnabled() = mutableMembers.filterShowSticker().all { it.radioClicked }

    Dialog(
        state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 750.dp, height = 600.dp),
        title = "Aufkleber",
        onCloseRequest = {},
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxHeight(.8f).padding(bottom = 8.dp)
            ) {
                LazyColumn(
                    state = lazyState,
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(.9f)
                ) {
                    item {
                        Text("Folgende Teilnehmer bekommen Aufkleber:", style = MaterialTheme.typography.subtitle1)
                    }
                    item {
                        Divider(modifier = Modifier.padding(vertical = 10.dp))
                    }
                    items(mutableMembers.filterShowSticker()) { member ->
                        val total = getTotalTrainingSessions(member, participations)
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Description(member, total)

                            RadioRecieved(member, mutableMembers)

                            Spacer(modifier = Modifier.width(8.dp))

                            RadioNotRecieved(member, mutableMembers)
                        }
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.fillMaxHeight().padding(top = 35.dp),
                    adapter = rememberScrollbarAdapter(
                        scrollState = lazyState
                    )
                )
            }
            Button(enabled = buttonEnabled(), modifier = Modifier.fillMaxWidth(.5f), onClick = {
                mutableMembers.filterShowSticker().forEach { member ->
                    if (member.stickerReceived) {
                        val nextStickerRecieved = stickerUnits.next(member.receivedStickerNumber).first
                        val nextStickerRecievedBy = "$nextStickerRecieved:${activeTrainer.id}:${LocalDate.now()}"

                        mutableMembers[mutableMembers.indexOf(member)] =
                            member.copy(
                                stickerReceivedBy = nextStickerRecievedBy,
                                radioClicked = false,
                                receivedStickerNumber = nextStickerRecieved,
                                stickerShowAgain = if (member.receivedStickerNumber == stickerUnits.keys.toList()[stickerUnits.keys.size - 2]) false else
                                    getTotalTrainingSessions(
                                        member,
                                        participations
                                    ) >= stickerUnits.next(stickerUnits.next(member.receivedStickerNumber).first).first // erster Teil vor dem && ist das gegenereignis von der if oben < / >=
                            )

                        updateSticker(
                            member,
                            nextStickerRecieved,
                            nextStickerRecievedBy
                        )
                    } else {
                        mutableMembers[mutableMembers.indexOf(member)] =
                            member.copy(
                                stickerShowAgain = false
                            )
                    }
                }

                if (mutableMembers.none { it.stickerShowAgain }) onDismiss()
            }) {
                Text("OK")
            }
        }
    }
}

@Composable
private fun RadioRecieved(
    student: Member,
    mutableMembers: SnapshotStateList<Member>
) {
    Text("Erhalten")
    RadioButton(
        student.stickerReceived && student.radioClicked,
        onClick = {
            mutableMembers[mutableMembers.indexOf(student)] =
                student.copy(
                    stickerReceived = true,
                    radioClicked = true
                )
        }
    )
}

@Composable
private fun RadioNotRecieved(
    student: Member,
    mutableMembers: SnapshotStateList<Member>
) {
    Text("Nicht erhalten")
    RadioButton(
        !student.stickerReceived && student.radioClicked,
        onClick = {
            mutableMembers[mutableMembers.indexOf(student)] =
                student.copy(
                    stickerReceived = false,
                    radioClicked = true
                )
        }
    )
}

@Composable
private fun Description(student: Member, total: Int) {
    val textModifier = Modifier.padding(8.dp).width(300.dp)
    if (student.receivedStickerNumber != stickerUnits.keys.last()) {
        if (
            student.receivedStickerNumber == stickerUnits.keys.elementAt(stickerUnits.keys.size - 2) ||
            total < student.receivedStickerNumber.nextStickerUnit().nextStickerUnit().first
        ) {
            Text(
                "${student.prename} ${student.surname}, hat " +
                        "$total Trainingseinheiten und bekommt einen " +
                        "${stickerUnits.next(student.receivedStickerNumber).second} Aufkleber",
                modifier = textModifier
            )
        } else {
            Text(
                "${student.prename} ${student.surname}, hat $total Trainingseinheiten und bekommt aber immer noch einen " +
                        "${stickerUnits.next(student.receivedStickerNumber).second} Aufkleber",
                modifier = textModifier
            )
        }
    }
}
