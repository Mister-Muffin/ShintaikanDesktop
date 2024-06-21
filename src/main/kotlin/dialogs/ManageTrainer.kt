package dialogs

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import composables.StudentList
import model.Member

@Composable
fun ManageTrainerDialog(members: List<Member>, setTrainerStatus: (Member, Boolean) -> Unit, onDismiss: () -> Unit) {

    val studentListTextWidth: Dp = 300.dp

    var searchFieldVal by remember { mutableStateOf("") }

    val lazyState = rememberLazyListState()

    val studentFilter = members.filter {
        (it.prename + it.surname)
            .lowercase()
            .contains(searchFieldVal.lowercase().replace(" ", ""))
    }

    Column(
        modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Space the button all the way to the bottom of the page
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Trainer verwalten", style = MaterialTheme.typography.h6)
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(.8f),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                        .width(studentListTextWidth + 30.dp), // use textWidth here to make them both the same width
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Aktuelle Trainer:", style = MaterialTheme.typography.h6)
                    Divider(modifier = Modifier.padding(4.dp))
                    CurrentTrainerList(members) { newVal, student ->
                        setTrainerStatus(student, newVal)

                    }
                }
                Column(
                    modifier = Modifier.padding(8.dp).width(studentListTextWidth + 30.dp), // +30 for scrollbar
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Trainer hinzufügen:", style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.padding(4.dp))
                    // Search field to select person as trainer
                    OutlinedTextField(
                        value = searchFieldVal,
                        onValueChange = { searchFieldVal = it },
                        leadingIcon = { Icon(Icons.Default.Search, "Search Icon") },
                        placeholder = {
                            Text(
                                "Suchen... (mind. 3 Zeichen)",
                                style = TextStyle.Default.copy(fontSize = 16.sp)
                            )
                        },
                        modifier = Modifier.padding(bottom = 10.dp).fillMaxWidth()
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
                                        StudentList(
                                            it.id,
                                            members,
                                            studentListTextWidth,
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
                                                checked = studentFilter[0].isTrainer,
                                                onCheckedChange = {
                                                    setTrainerStatus(studentFilter[0], it)

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
        }
        Button(onClick = onDismiss, modifier = Modifier.padding(8.dp).width(studentListTextWidth)) {
            Text("OK")
        }
    }
}

@Composable
private fun CurrentTrainerList(
    members: List<Member>,
    onCheckedChange: (newVal: Boolean, member: Member) -> Unit
) {
    val lazyState = rememberLazyListState()
    Row {
        LazyColumn(state = lazyState) {
            items(members.filter { it.isTrainer }) { student ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(student.prename + " " + student.surname, style = MaterialTheme.typography.body1)
                    Checkbox(
                        checked = student.isTrainer,
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
