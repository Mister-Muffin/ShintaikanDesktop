package dialogs

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import getTotalTrainingSessions
import models.Member
import models.Trainer
import models.editMemberSticker
import models.loadTeilnahme
import next
import stickerUnits
import java.time.LocalDate

@Composable
fun stickerDialog(
    stickerStudentsList: List<Member>,
    activeTrainer: Trainer,
    onDismiss: () -> Unit
) {

    val mutableMembers = remember { mutableStateListOf<Member>() }
    remember {
        for (student in stickerStudentsList) {
            mutableMembers.add(student)
        }
    }

    val lazyState = rememberLazyListState()

    val teilnahme = loadTeilnahme()

    /**
     * This function returns true if all radio buttons have been clicked at lease once to ensure,
     * that the user has made his desicion for each student
     */
    fun buttonEnabled() = mutableMembers.all { it.radioClicked }

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
                    item { Divider(modifier = Modifier.padding(vertical = 10.dp)) }
                    items(mutableMembers) { student ->
                        val total = getTotalTrainingSessions(student, teilnahme)
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            description(student, total)

                            radioRecieved(student, mutableMembers, total)

                            Spacer(modifier = Modifier.width(8.dp))

                            radioNotRecieved(student, mutableMembers)
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
                try {
                    mutableMembers.forEach { member ->
                        val nextStickerRecieved = stickerUnits.next(member.sticker_recieved).first
                        val nextStickerRecievedBy = "$nextStickerRecieved:${activeTrainer.id}:${LocalDate.now()}"
                        if (member.stickerRecieved) {
                            editMemberSticker(
                                member.copy(
                                    sticker_recieved_by = nextStickerRecievedBy,
                                    sticker_recieved = nextStickerRecieved
                                )
                            )

                        }
                        if (member.sticker_recieved != stickerUnits.keys.toList()[stickerUnits.keys.size - 1]) {
                            mutableMembers[mutableMembers.indexOf(member)] =
                                member.copy(
                                    sticker_recieved_by = nextStickerRecievedBy,
                                    radioClicked = false,
                                    sticker_recieved = nextStickerRecieved
                                )
                        }
                        /* Die geänderten 'members' müssen zurückgegeben werden, da bei den 'member', die
                         nochmal aufgelistet werden sollen, 'sticker_show_again' auf true gesetzt wird.
                         Würden die Mitglieder hier nicht zurückgegeben werden,
                         sondern die Daten neu geladen werden, wäre diese Eigenschaft (sticker_show_again)
                         wieder false, und der Dialog würde erst bei der nächsten Trainingseintragung
                         wieder kommen.
                         */

                        val tmp: MutableList<Member> = mutableListOf()
                        mutableMembers.forEach { tmp.add(it) }
                        mutableMembers.clear()
                        tmp.forEach {
                            if (it.sticker_show_again) {
                                mutableMembers.add(member)
                            }
                        }
                        if (tmp.isEmpty()) {
                            onDismiss()
                        } else {
                            mutableMembers.clear()
                            tmp.forEach { mutableMembers.add(it) }
                        }
                    }
                } catch (e: java.util.ConcurrentModificationException) {
                    println("WARNING: Error catched")
                }
            }) {
                Text("OK")
            }
        }
    }
}

@Composable
private fun radioRecieved(
    student: Member,
    mutableMembers: SnapshotStateList<Member>,
    total: Int
) {
    Text("Erhalten")
    RadioButton(
        student.stickerRecieved && student.radioClicked,
        onClick = {
            mutableMembers[mutableMembers.indexOf(student)] =
                student.copy(
                    stickerRecieved = true,
                    radioClicked = true,
                    sticker_show_again = if (student.sticker_recieved == stickerUnits.keys.toList()[stickerUnits.keys.size - 2]) false else
                        total >= stickerUnits.next(stickerUnits.next(student.sticker_recieved).first).first // erster Teil vor dem && ist das gegenereignis von der if oben < / >=
                )
        })
}

@Composable
private fun radioNotRecieved(
    student: Member,
    mutableMembers: SnapshotStateList<Member>
) {
    Text("Nicht erhalten")
    RadioButton(
        !student.stickerRecieved && student.radioClicked,
        onClick = {
            mutableMembers[mutableMembers.indexOf(student)] =
                student.copy(
                    stickerRecieved = false,
                    radioClicked = true,
                    sticker_show_again = false
                )
        })
}

@Composable
private fun description(student: Member, total: Int) {
    val textModifier = Modifier.padding(8.dp).width(300.dp)
    if (student.sticker_recieved != stickerUnits.keys.last()) {
        if (
            student.sticker_recieved == stickerUnits.keys.toList()[stickerUnits.keys.size - 2] ||
            total < stickerUnits.next(stickerUnits.next(student.sticker_recieved).first).first
        ) {
            Text(
                "${student.prename} ${student.surname}, hat " +
                        "$total Trainingseinheiten und bekommt einen " +
                        "${stickerUnits.next(student.sticker_recieved).second} Aufkleber",
                modifier = textModifier
            )
        } else {
            Text(
                "${student.prename} ${student.surname}, hat $total Trainingseinheiten und bekommt aber immer noch einen " +
                        "${stickerUnits.next(student.sticker_recieved).second} Aufkleber",
                modifier = textModifier
            )
        }
    }
}
