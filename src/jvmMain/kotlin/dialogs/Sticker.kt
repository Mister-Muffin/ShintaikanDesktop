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
import models.Student
import models.Trainer
import models.editStudentSticker
import models.loadTeilnahme
import next
import stickerUnits

@Composable
fun stickerDialog(
    stickerStudentsList: List<Student>,
    activeTrainer: Trainer,
    onDismiss: (students: List<Student>) -> Unit
) {

    val mutableStudents = remember { mutableStateListOf<Student>() }
    remember {
        for (student in stickerStudentsList) {
            mutableStudents.add(student)
        }
    }

    val teilnahme = loadTeilnahme()


    /**
     * This function returns true if all radio buttons have been clicked at lease once to ensure,
     * that the user has made his desicion for each student
     */
    fun buttonEnabled(): Boolean {
        mutableStudents.forEach { s ->
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
                items(mutableStudents) { student ->
                    val total = getTotalTrainingSessions(student, teilnahme)
                    LazyRow(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            val nextStickerName = stickerUnits.next(student.sticker_recieved).second
                            if (
                                student.sticker_recieved == stickerUnits.keys.toList()[stickerUnits.keys.size - 2] ||
                                total < stickerUnits.next(stickerUnits.next(student.sticker_recieved).first).first
                            ) {
                                Text(
                                    "${student.prename} ${student.surname}, hat " +
                                            "$total Trainingseinheiten und bekommt einen " +
                                            "$nextStickerName Aufkleber",
                                    modifier = Modifier.padding(8.dp).width(300.dp)
                                )
                            } else {
                                Text(
                                    "${student.prename} ${student.surname}, hat $total Trainingseinheiten und bekommt aber immer noch einen " +
                                            "$nextStickerName Aufkleber",
                                    modifier = Modifier.padding(8.dp).width(300.dp)
                                )
                            }
                        }
                        item {
                            Text("Erhalten")
                            RadioButton(
                                student.stickerRecieved && student.radioClicked,
                                onClick = {
                                    mutableStudents[mutableStudents.indexOf(student)] =
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
                                    mutableStudents[mutableStudents.indexOf(student)] =
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
                mutableStudents.forEach { s ->
                    if (s.stickerRecieved) {
                        editStudentSticker(
                            s.copy(
                                sticker_recieved_by = activeTrainer.id,
                                //sticker_animal = stickerUnitNames[stickerUnits.indexOf(s.sticker_recieved) + 1],
                                sticker_recieved = stickerUnits.next(s.sticker_recieved).first
                            )
                        )

                    }
                    if (s.sticker_recieved != stickerUnits.keys.toList()[stickerUnits.keys.size - 1]) {
                        mutableStudents[mutableStudents.indexOf(s)] =
                            s.copy(
                                sticker_recieved_by = activeTrainer.id,
                                radioClicked = false,
                                //sticker_animal = stickerUnitNames[stickerUnits.indexOf(s.sticker_recieved) + 1],
                                sticker_recieved = stickerUnits.next(s.sticker_recieved).first
                            )
                    }

                    onDismiss(mutableStudents)
                }
            }) {
                Text("OK")
            }
        }
    }

}