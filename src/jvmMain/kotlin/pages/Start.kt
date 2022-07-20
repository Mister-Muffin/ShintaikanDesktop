package pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import models.Message
import models.Student
import models.addMessage
import models.deleteMessage
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@Composable
fun startPage(students: List<Student>, messages: List<Message>, changeScreen: (id: Int) -> Unit) {

    val allStudents = remember { mutableStateListOf<Student>() }
    remember {
        for (student in students) {
            allStudents.add(student)
        }
    }
    val allMessages = remember { mutableStateListOf<Message>() }
    remember {
        for (message in messages) {
            allMessages.add(message)
        }
    }
    val showDeleteMessageDialog = remember { mutableStateOf(false) }

    val birthdays = remember { loadBirthdays(students) }

    val newMessage = remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(all = 8.dp)) {
        if (showDeleteMessageDialog.value) deleteDialog(
            messages = allMessages,
            onDismiss = { showDeleteMessageDialog.value = false })
        Text(
            "Willkommen!",
            style = TextStyle(color = Color(0xffff8f06), fontSize = 30.sp),
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Monospace,
        )
        Divider(
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Box() {
            buttonRow(changeScreen)
        }
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxSize().padding(top = 24.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(0.5F),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    headerText("Es haben/hatten Geburtstag:")
                }
                items(birthdays) {
                    Row {
                        val period = Period.between(it.birthday, LocalDate.now()).days
                        Text(
                            text = "${it.surname}, ${it.prename}: ",
                            fontWeight = FontWeight.Normal,
                        )
                        @Suppress("KotlinConstantConditions")
                        Text(
                            text = if (period == 1) {
                                "gestern"
                            } else if (period <= 2) {
                                "vor ${period} Tagen"
                            } else if (period == -1) {
                                "morgen"
                            } else if (period >= -2) {
                                "in ${period * (-1)} Tagen"
                            } else {
                                "heute \uD83E\uDD73"
                            },
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Divider(modifier = Modifier.fillMaxHeight().width(1.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                headerText(text = "Kurznachrichten")

                Row {
                    OutlinedTextField(
                        value = newMessage.value,
                        modifier = Modifier.fillMaxWidth(0.8F),
                        trailingIcon = {
                            IconButton(onClick = {
                                val newMessageObj = Message(-1, newMessage.value, "", LocalDate.now())
                                val id = addMessage(newMessageObj)
                                allMessages.add(
                                    Message(
                                        id = id,
                                        message = newMessage.value,
                                        short = "",
                                        newMessageObj.dateCreated
                                    )
                                )
                                newMessage.value = ""
                            }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        },
                        singleLine = true,
                        onValueChange = { newMessage.value = it }
                    )
                    IconButton(onClick = { showDeleteMessageDialog.value = true }) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp, start = 24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    items(allMessages.sortedBy { it.dateCreated }) { message(it) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun deleteDialog(messages: MutableList<Message>, onDismiss: () -> Unit) {
    Dialog(
        state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 750.dp, height = 600.dp),
        title = "Nachrichten löschen",
        onCloseRequest = onDismiss
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            headerText("Nachichten löschen")
            LazyColumn(horizontalAlignment = Alignment.Start) {
                items(messages) {
                    Row {
                        Text(text = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(it.dateCreated).toString() + ": ")
                        Text(text = it.message.slice(0 until it.message.length.coerceAtMost(30)) + "...")
                        IconButton(modifier = Modifier.size(20.dp), onClick = {
                            deleteMessage(it.id)
                            messages.remove(it)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    }
                    Divider()
                }
            }
        }
    }

}

@Composable
private fun message(message: Message) {
    Row {
        Text(text = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(message.dateCreated).toString() + ": ")
        Text(text = message.message)
    }
}

@Composable
private fun headerText(text: String) {
    Text(text = text, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
}

@Composable
private fun buttonRow(changeScreen: (id: Int) -> Unit) {
    @Composable
    fun rowButton(text: String, onClick: () -> Unit, enabled: Boolean) {
        Button(
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray, contentColor = Color.White),
            //modifier = Modifier.width(150.dp).height(40.dp),
            onClick = onClick
        ) {
            Text(text)
        }
    }

    Row {
        rowButton(text = "Teilnehmer eintragen", enabled = true, onClick = { changeScreen(1) })
    }
}

private fun loadBirthdays(students: List<Student>): MutableList<Student> {
    val birthdays = mutableListOf<Student>()

    for (student in students) {
        if (student.birthday == null) continue
        if (student.birthday >= (LocalDate.now().minusDays(3)) &&
            student.birthday <= LocalDate.now().plusDays(3)
        ) {
            birthdays.add(student)
        }
    }
    return birthdays
}