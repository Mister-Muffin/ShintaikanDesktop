package pages

import Screen
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import model.Member
import model.Message
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import kotlin.time.Duration

@Composable
fun StartPage(
    members: List<Member>,
    messages: List<Message>,
    birthdays: List<Member>,
    lastImport: String,
    addMessage: (newMessage: String) -> Unit,
    deleteMessage: (id: Int) -> Unit,
    updateMessage: (Message) -> Unit,
    startupTime: () -> Duration,
    changeScreen: (id: Screen) -> Unit
) {

    var newMessage by remember { mutableStateOf("") }

    val lazyMessagesListState = rememberLazyListState()

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(all = 8.dp).fillMaxSize()) {

        if (members.isEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("Bitte warten...")
            }
        } else {
            Text("Willkommen", style = MaterialTheme.typography.h1)
            Text("Heute ist der ${DateTimeFormatter.ofPattern("dd.MM.yyyy").format(LocalDate.now())}")
            Divider(
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Button(
                modifier = Modifier.width(COMPONENT_WIDTH.dp),
                onClick = { changeScreen(Screen.SELECT_TRAINER) }
            ) {
                Text(text = "Teilnehmer eintragen")
                Icon(Icons.Default.ArrowForward, "", modifier = Modifier.padding(start = 8.dp))
            }

            Text(
                "Jederzeit zurück zum Startbildschrim mit 'ESC'",
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxSize(.95f).padding(top = 24.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxHeight().fillMaxWidth(0.5F),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            "Es haben/hatten Geburtstag:",
                            style = MaterialTheme.typography.subtitle1,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(birthdays) {
                        Row {
                            val birthday = it.birthday.plusYears(((LocalDate.now().year - it.birthday.year).toLong()))
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
                                    "heute!"
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
                    Text(
                        text = "Kurznachrichten",
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Row {
                        OutlinedTextField(
                            value = newMessage,
                            placeholder = {
                                Text(
                                    "Kurznachicht eingeben...",
                                    style = TextStyle.Default.copy(fontSize = 16.sp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(0.8F).onPreviewKeyEvent {
                                // submit new message when either "ctrl" or "shift" is pressed
                                // together with "Enter"
                                if (((it.isCtrlPressed || it.isShiftPressed) && it.key == Key.Enter && it.type == KeyEventType.KeyUp)) {
                                    addMessage(newMessage)
                                    newMessage = ""
                                    true
                                } else false
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    addMessage(newMessage)
                                    newMessage = ""
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            },
                            singleLine = false,
                            onValueChange = { newMessage = it },
                        )
                    }
                    Text(
                        "CTRL / SHIFT + Enter oder '+' zum erstellen drücken",
                        style = MaterialTheme.typography.caption
                    )
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp, start = 24.dp)) {
                        LazyColumn(
                            horizontalAlignment = Alignment.Start,
                            state = lazyMessagesListState
                        ) {
                            items(messages.sortedBy { it.dateCreated }) {
                                MessageView(it, { deleteMessage(it.id) }, updateMessage)
                            }
                        }
                        VerticalScrollbar(
                            modifier = Modifier.fillMaxHeight().requiredWidth(10.dp).offset(8.dp),
                            adapter = rememberScrollbarAdapter(scrollState = lazyMessagesListState)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize().padding(8.dp)
            ) {
                Text(
                    "Letzter Datenimport: $lastImport"
                )

                Text(startupTime().toString())

                Text(
                    try {
                        val dateString: String =
                            useResource("buildDate.txt") { it.readBytes().toString(Charsets.UTF_8) }
                        "Programmversion vom: $dateString"
                    } catch (e: java.io.FileNotFoundException) {
                        "N/A"
                    }
                )
            }

        }
    }
}

@Composable
private fun MessageView(message: Message, deleteMessage: () -> Unit, updateMessage: (Message) -> Unit) {

    var showEditMessageDialog by remember { mutableStateOf(false) }

    if (showEditMessageDialog) EditMessageDialog(message, updateMessage) { showEditMessageDialog = false; }
    LazyRow(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(.9f)) {
        item {
            Text(text = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(message.dateCreated).toString() + ": ")
            Text(text = message.message, modifier = Modifier.fillParentMaxWidth(.6f))
        }
        item {
            Icon(Icons.Default.Edit, null, modifier = Modifier.padding(2.dp).clickable {
                showEditMessageDialog = true
            })
            Icon(Icons.Default.Delete, null, modifier = Modifier.padding(2.dp).clickable {
                deleteMessage()
            })
        }
    }
}

@Composable
private fun EditMessageDialog(message: Message, updateMessage: (Message) -> Unit, close: () -> Unit) {

    var textFieldValue by remember { mutableStateOf(message.message) }

    Dialog(
        state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 600.dp, height = 250.dp),
        title = "Kurznachricht bearbeiten",
        onCloseRequest = close
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(textFieldValue, onValueChange = { textFieldValue = it })
            Button(onClick = {
                updateMessage(message.copy(message = textFieldValue))
                close()
            }) {
                Text("Nachicht ändern")
            }
        }
    }
}
