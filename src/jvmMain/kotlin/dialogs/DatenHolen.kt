package dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun datenHolenWindow(onDismiss: () -> Unit) {
    val res = remember { mutableStateOf("") }
    Dialog(
        state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 750.dp, height = 600.dp),
        title = "Daten holen",
        onCloseRequest = onDismiss
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val process = Runtime.getRuntime().exec("src/pythonMain/venv/bin/python src/pythonMain/main.py")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readText();

            val exitVal = process.waitFor()
            if (exitVal == 0) {
                Text(line)
            } else {
                Text("Error")
            }

        }
    }

}