package dialogs

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import composables.StudentList
import models.Student
import models.editIsTrainer
import models.loadStudents

@Composable
fun manageTrainerDialog(students1: List<Student>, onDismiss: () -> Unit) {

    val students = remember { mutableStateListOf<Student>() }
    remember {
        for (student in students1) {
            students.add(student)
        }
    }

    var requirePassword by remember { mutableStateOf(true) }
    var searchFieldVal by remember { mutableStateOf("") }

    val lazyState = rememberLazyListState()

    val studentFilter = students.filter {
        (it.prename + it.surname)
            .lowercase()
            .contains(searchFieldVal.lowercase().replace(" ", ""))
    }

    if (requirePassword) {
        passwordDialog(
            result = { pwCorrect -> requirePassword = !pwCorrect }, // if password correct, set requirePasswort to false
            onDissmiss = onDismiss
        )
    } else {
        Dialog(
            state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 750.dp, height = 600.dp),
            title = "Trainer verwalten",
            onCloseRequest = onDismiss
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(.8f).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Aktuelle Trainer:", style = MaterialTheme.typography.h6)
                    currentTrainerList(students) { newVal, student ->
                        editIsTrainer(student.id, newVal)
                        students.clear()
                        for (s in loadStudents()) {
                            students.add(s)
                        }
                    }
                    // Search field to select person as trainer
                    OutlinedTextField(
                        value = searchFieldVal,
                        onValueChange = { searchFieldVal = it },
                        placeholder = { Text("Suchen... (mind. 3 Zeichen)") },
                        modifier = Modifier.padding(bottom = 10.dp).width(300.dp)
                    )
                    Row {
                        LazyColumn(state = lazyState) {
                            if (searchFieldVal.length > 2) {
                                if (studentFilter.size >= 2) {
                                    items(students.filter {
                                        (it.prename + it.surname)
                                            .lowercase()
                                            .contains(searchFieldVal.lowercase().replace(" ", ""))
                                    }) {
                                        StudentList().studentList(
                                            it.id,
                                            students,
                                            onClick = { nameString -> searchFieldVal = nameString })
                                    }
                                } else if (studentFilter.size == 1) {
                                    item {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "${studentFilter[0].prename} ${studentFilter[0].surname}",
                                                style = MaterialTheme.typography.body1
                                            )
                                            Checkbox(
                                                checked = studentFilter[0].is_trainer,
                                                onCheckedChange = {
                                                    editIsTrainer(studentFilter[0].id, it)
                                                    students.clear()
                                                    for (s in loadStudents()) {
                                                        students.add(s)
                                                    }
                                                    searchFieldVal = ""
                                                })
                                        }
                                    }
                                } else {
                                    item { Text("Keine Personen gefunden") }
                                }
                            }
                        }
                        VerticalScrollbar(
                            modifier = Modifier.fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(
                                scrollState = lazyState
                            )
                        )
                    }
                }
                Button(onClick = onDismiss, modifier = Modifier.padding(8.dp).width(StudentList.textWidth)) {
                    Text("OK")
                }
            }
        }
    }
}

@Composable
private fun currentTrainerList(
    students: MutableList<Student>,
    onCheckedChange: (newVal: Boolean, student: Student) -> Unit
) {
    LazyColumn {
        items(students.filter { it.is_trainer }) { student ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(student.prename, style = MaterialTheme.typography.body1)
                Checkbox(
                    checked = student.is_trainer,
                    onCheckedChange = { onCheckedChange(it, student) }
                )
            }
        }
    }
}
