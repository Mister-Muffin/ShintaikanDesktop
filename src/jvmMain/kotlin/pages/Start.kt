package pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import models.*
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@Composable
fun startPage(changeScreen: (id: Int) -> Unit) {
    val students = loadStudents()
    val messages = loadMessages()
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

    val birthdays = remember { loadBirthdays(students) }

    val newMessage = remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(all = 8.dp)) {

        Text("Willkommen", style = MaterialTheme.typography.h1)
        Divider(
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Row {
            Button(
                modifier = Modifier.width(250.dp),
                onClick = { changeScreen(1) }
            ) {
                Text(text = "Teilnehmer eintragen")
            }
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
                    Text("Es haben/hatten Geburtstag:", style = MaterialTheme.typography.subtitle1)
                }
                items(birthdays) {
                    Row {
                        val birthday = it.birthday!!.plusYears(((LocalDate.now().year - it.birthday.year).toLong()))
                        val period = Period.between(LocalDate.now(), birthday).days
                        Text(
                            text = "${it.surname}, ${it.prename}: ",
                            fontWeight = FontWeight.Normal,
                        )
                        Text(
                            text = if (period == 1) {
                                "morgen"
                            } else if (period in 1..3) {
                                "in $period Tagen"
                            } else if (period == -1) {
                                "gestern"
                            } else if (period >= -3 && period < 0) {
                                "vor ${period * (-1)} Tagen"
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
                Text(text = "Kurznachrichten", style = MaterialTheme.typography.subtitle1)

                Row {
                    OutlinedTextField(
                        value = newMessage.value,
                        placeholder = {
                            Text(
                                "Kurznachicht eingeben...",
                                style = TextStyle.Default.copy(fontSize = 16.sp)
                            )
                        },
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
                }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp, start = 24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    items(allMessages.sortedBy { it.dateCreated }) { message(it) { allMessages.remove(it) } }
                }
            }
        }
    }
}

@Composable
private fun message(message: Message, onMessageDeleted: (message: Message) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(.9f)) {
        item {
            Text(text = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(message.dateCreated).toString() + ": ")
            Text(text = message.message)
        }
        item {
            Icon(Icons.Default.Edit, null, modifier = Modifier.padding(2.dp).clickable {

            })
            Icon(Icons.Default.Delete, null, modifier = Modifier.padding(2.dp).clickable {
                deleteMessage(message.id)
                onMessageDeleted(message)
            })
        }
    }
}


private fun loadBirthdays(students: List<Student>): MutableList<Student> {
    val birthdays = mutableListOf<Student>()

    for (student in students) {
        if (student.birthday == null) continue
        val birthday = student.birthday.plusYears((LocalDate.now().year - student.birthday.year).toLong())
        if (birthday >= (LocalDate.now().minusDays(3)) &&
            birthday <= LocalDate.now().plusDays(3)
        ) {
            birthdays.add(student)
        }
    }
    return birthdays
}