package dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import getTotalTrainingSessions
import models.Member
import models.Trainer
import models.editStudentSticker
import models.loadTeilnahme
import next
import stickerUnits
import java.time.LocalDate

@Composable
fun stickerDialog(
    stickerStudentsList: List<Member>,
    activeTrainer: Trainer,
    onDismiss: (members: List<Member>) -> Unit
) {

    val mutableMembers = remember { mutableStateListOf<Member>() }
    remember {
        for (student in stickerStudentsList) {
            mutableMembers.add(student)
        }
    }

    val teilnahme = loadTeilnahme()


    /**
     * This function returns true if all radio buttons have been clicked at lease once to ensure,
     * that the user has made his desicion for each student
     */
    fun buttonEnabled(): Boolean {
        mutableMembers.forEach { s ->
            if (!s.radioClicked) {
                return false
            }
        }
        return true
    }

    Dialog(
        state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 750.dp, height = 600.dp),
        title = "Aufkleber",
        onCloseRequest = {},
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxHeight(.8f).padding(bottom = 8.dp)
            ) {
                item {
                    Text("Folgende Teilnehmer bekommen Aufkleber:", style = MaterialTheme.typography.subtitle1)
                }
                item { Divider(modifier = Modifier.padding(vertical = 10.dp)) }
                items(mutableMembers) { student ->
                    val total = getTotalTrainingSessions(student, teilnahme)
                    LazyRow(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            if (student.sticker_recieved != stickerUnits.keys.last()) {
                                if (
                                    student.sticker_recieved == stickerUnits.keys.toList()[stickerUnits.keys.size - 2] ||
                                    total < stickerUnits.next(stickerUnits.next(student.sticker_recieved).first).first
                                ) {
                                    Text(
                                        "${student.prename} ${student.surname}, hat " +
                                                "$total Trainingseinheiten und bekommt einen " +
                                                "${stickerUnits.next(student.sticker_recieved).second} Aufkleber",
                                        modifier = Modifier.padding(8.dp).width(300.dp)
                                    )
                                } else {
                                    Text(
                                        "${student.prename} ${student.surname}, hat $total Trainingseinheiten und bekommt aber immer noch einen " +
                                                "${stickerUnits.next(student.sticker_recieved).second} Aufkleber",
                                        modifier = Modifier.padding(8.dp).width(300.dp)
                                    )
                                }
                            }
                        }
                        item {
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
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nicht erhalten", style = MaterialTheme.typography.body1)
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
                    }
                }
            }
            Button(enabled = buttonEnabled(), modifier = Modifier.fillMaxWidth(.5f), onClick = {
                mutableMembers.forEach { s ->
                    val nextStickerRecieved = stickerUnits.next(s.sticker_recieved).first
                    val nextStickerRecievedBy = "$nextStickerRecieved:${activeTrainer.id}:${LocalDate.now()}"
                    if (s.stickerRecieved) {
                        editStudentSticker(
                            s.copy(
                                sticker_recieved_by = nextStickerRecievedBy,
                                //sticker_animal = stickerUnitNames[stickerUnits.indexOf(s.sticker_recieved) + 1],
                                sticker_recieved = nextStickerRecieved
                            )
                        )

                    }
                    if (s.sticker_recieved != stickerUnits.keys.toList()[stickerUnits.keys.size - 1]) {
                        mutableMembers[mutableMembers.indexOf(s)] =
                            s.copy(
                                sticker_recieved_by = nextStickerRecievedBy,
                                radioClicked = false,
                                //sticker_animal = stickerUnitNames[stickerUnits.indexOf(s.sticker_recieved) + 1],
                                sticker_recieved = nextStickerRecieved
                            )
                    }

                    onDismiss(mutableMembers)
                }
            }) {
                Text("OK")
            }
        }
    }

}