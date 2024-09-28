package dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import format
import model.Member
import model.Participation

@Composable
fun AddNoteDialog(note: String, onDismiss: (note: String, save: Boolean) -> Unit) {

    var noteFieldText by remember { mutableStateOf(note) }

    DialogWindow(
        state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 750.dp, height = 600.dp),
        title = "Notiz",
        onCloseRequest = { onDismiss("", false) },
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Notiz hinzufügen:", style = MaterialTheme.typography.subtitle1)
            OutlinedTextField(
                noteFieldText,
                { noteFieldText = it },
                placeholder = {
                    Text("Notiz hier eingeben...", style = TextStyle.Default.copy(fontSize = 16.sp))
                },
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Button(
                onClick = { onDismiss(noteFieldText, true) }
            ) {
                Text("Speichern")
                Icon(Icons.Default.Save, "", modifier = Modifier.padding(start = 8.dp))
            }
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Info, null, modifier = Modifier.padding(end = 4.dp))
                Text("Die Notizen können unter Mitglieder -> Daten abfragen angezeigt werden.")
            }
        }
    }
}

@Composable
fun ViewNotesDialog(member: Member?, participationsWithNotes: List<Participation>, onDismiss: () -> Unit) {
    DialogWindow(
        state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 750.dp, height = 600.dp),
        title = "Notizen anzeigen",
        onCloseRequest = { onDismiss() },
    ) {
        if (member == null) onDismiss()
        else {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Notizen zu ${member.prename}:", style = MaterialTheme.typography.subtitle1)
                Spacer(modifier = Modifier.height(8.dp))
                participationsWithNotes.forEach { participation ->
                    val formattedDate = participation.date.format()
                    Text("$formattedDate: ${participation.note}")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDismiss
                ) {
                    Text("Schließen")
                    Icon(Icons.Default.Close, "", modifier = Modifier.padding(start = 8.dp))
                }
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, null, modifier = Modifier.padding(end = 4.dp))
                    Text("Neue Notizen auf der rechten Seite der Teilnehmerliste hinzufügen.")
                }
            }
        }
    }
}
