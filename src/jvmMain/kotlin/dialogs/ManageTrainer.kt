package dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState

@Composable
fun manageTrainerDialog(onDismiss: () -> Unit) {

    var requirePassword by remember { mutableStateOf(true) }

    if (requirePassword) {
        passwordDialog { pwCorrect -> requirePassword = !pwCorrect } // if password correct, set requirePasswort to false
    } else {
        Dialog(
            state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 750.dp, height = 600.dp),
            title = "Letzte Prüfungen abfragen",
            onCloseRequest = onDismiss
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            }
        }
    }
}