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
import models.Student
import models.Trainer
import models.editStudentSticker
import stickerUnitNames
import stickerUnits

@Composable
fun stickerDialog(stickerStudentsList: List<Student>, activeTrainer: Trainer, onDismiss: () -> Unit) {

    val mutableStudents = remember { mutableStateListOf<Student>() }
    remember {
        for (student in stickerStudentsList) {
            mutableStudents.add(student)
        }
    }


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
                    LazyRow(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            if (stickerUnits[stickerUnits.indexOf(student.sticker_units) - 1] == student.sticker_recieved) {
                                Text(
                                    "${student.prename} ${student.surname}, hat mehr als ${student.sticker_units} Trainingseinheiten und bekommt einen ${student.sticker_animal} Aufkleber",
                                    modifier = Modifier.padding(8.dp).width(300.dp)
                                )
                            } else {
                                Text(
                                    "${student.prename} ${student.surname}, hat mehr als ${student.sticker_units} Trainingseinheiten und bekommt aber immer noch einen ${
                                        stickerUnitNames[stickerUnits.indexOf(
                                            student.sticker_recieved
                                        ) + 1]
                                    } Aufkleber",
                                    modifier = Modifier.padding(8.dp).width(300.dp)
                                )
                            }
                        }
                        item {
                            Text("Erhalten")
                            RadioButton(
                                student.sticker_recieved == stickerUnits[stickerUnits.indexOf(student.sticker_old_unit) + 1] && student.radioClicked,
                                onClick = {
                                    mutableStudents[mutableStudents.indexOf(student)] =
                                        student.copy(
                                            sticker_recieved = stickerUnits[stickerUnits.indexOf(student.sticker_old_unit) + 1],
                                            radioClicked = true
                                        )
                                })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nicht erhalten", style = MaterialTheme.typography.body1)
                            RadioButton(
                                student.sticker_recieved == stickerUnits[stickerUnits.indexOf(student.sticker_old_unit)] && student.radioClicked,
                                onClick = {
                                    mutableStudents[mutableStudents.indexOf(student)] =
                                        student.copy(
                                            sticker_recieved = stickerUnits[stickerUnits.indexOf(student.sticker_old_unit)],
                                            radioClicked = true
                                        )
                                })
                        }
                    }
                }
            }
            Button(enabled = buttonEnabled(), modifier = Modifier.fillMaxWidth(.5f), onClick = {
                mutableStudents.forEach { s ->
                    if (s.sticker_recieved == stickerUnits[stickerUnits.indexOf(s.sticker_old_unit) + 1]) {
                        editStudentSticker(
                            s.copy(
                                sticker_units = stickerUnits[stickerUnits.indexOf(s.sticker_units) + 1],
                                sticker_recieved_by = activeTrainer.id,
                                sticker_animal = stickerUnitNames[stickerUnits.indexOf(s.sticker_units) + 1],
                                sticker_recieved = stickerUnits[stickerUnits.indexOf(s.sticker_old_unit)]
                            )
                        )
                    }

                    onDismiss()
                }
            }) {
                Text("OK")
            }
        }
    }

}