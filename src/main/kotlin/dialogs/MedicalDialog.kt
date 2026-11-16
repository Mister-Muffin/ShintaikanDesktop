package dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import model.Member

@Composable
fun ViewMedicalDialog(member: Member?, onDismiss: () -> Unit) {
    DialogWindow(
        state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 750.dp, height = 600.dp),
        title = "Medizinische Informationen anzeigen",
        onCloseRequest = { onDismiss() },
    ) {
        if (member == null) onDismiss()
        else {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Medizinische Informationen zu ${member.prename}:", style = MaterialTheme.typography.subtitle1)

                Spacer(modifier = Modifier.height(8.dp))

                Text(member.healthCondition ?: "Keine medizinischen Informationen vorhanden.")

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss
                ) {
                    Text("Schließen")
                    Icon(Icons.Default.Close, "", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
