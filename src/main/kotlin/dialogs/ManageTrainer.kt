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
import composables.StudentList
import kotlinx.coroutines.launch
import models.Member
import models.editIsTrainer
import models.loadMembers

@Composable
fun manageTrainerDialog(students1: List<Member>, onDismiss: () -> Unit) {

    val scope = rememberCoroutineScope()

    val members = remember { mutableStateListOf<Member>() }
    remember {
        for (student in students1) {
            members.add(student)
        }
    }

    var requirePassword by remember { mutableStateOf(true) }
    var searchFieldVal by remember { mutableStateOf("") }

    val lazyState = rememberLazyListState()

    val studentFilter = members.filter {
        (it.prename + it.surname)
            .lowercase()
            .contains(searchFieldVal.lowercase().replace(" ", ""))
    }

    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(.8f).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
                    .width(StudentList.textWidth + 30.dp), // use textWidth here to make them both the same width
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Aktuelle Trainer:", style = MaterialTheme.typography.h6)
                Divider(modifier = Modifier.padding(4.dp))
                currentTrainerList(members) { newVal, student ->
                    editIsTrainer(student.id, newVal)
                    scope.launch {
                        reloadMembers(members)
                    }
                }
            }
            Column(
                modifier = Modifier.padding(8.dp).width(StudentList.textWidth + 30.dp), // +30 for scrollbar
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Trainer hinzufügen:", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.padding(4.dp))
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
                                items(members.filter {
                                    (it.prename + it.surname)
                                        .lowercase()
                                        .contains(searchFieldVal.lowercase().replace(" ", ""))
                                }) {
                                    StudentList().studentList(
                                        it.id,
                                        members,
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
                                                scope.launch {
                                                    reloadMembers(members)
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
                        modifier = Modifier.fillMaxHeight().width(8.dp).padding(start = 2.dp),
                        adapter = rememberScrollbarAdapter(
                            scrollState = lazyState
                        )
                    )
                }
            }
        }
        Button(onClick = onDismiss, modifier = Modifier.padding(8.dp).width(StudentList.textWidth)) {
            Text("OK")
        }
    }
}

private suspend fun reloadMembers(members: MutableList<Member>) {
    members.clear()
    members.addAll(loadMembers())
}

@Composable
private fun currentTrainerList(
    members: MutableList<Member>,
    onCheckedChange: (newVal: Boolean, member: Member) -> Unit
) {
    val lazyState = rememberLazyListState()
    Row {
        LazyColumn(state = lazyState) {
            items(members.filter { it.is_trainer }) { student ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(student.prename + " " + student.surname, style = MaterialTheme.typography.body1)
                    Checkbox(
                        checked = student.is_trainer,
                        onCheckedChange = { onCheckedChange(it, student) }
                    )
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
