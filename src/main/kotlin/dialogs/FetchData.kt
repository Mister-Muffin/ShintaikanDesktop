package dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import composables.*

private const val fileExtension = ".csv"

@Composable
fun FetchDataWindow(
    window: ComposeWindow,
    fetchData: (csvPath: String, setText: (String) -> Unit, onComplete: () -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var textFieldValue by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(States.NO_FILE_SELECTED) }

    var path by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Daten holen", style = MaterialTheme.typography.h6)
        Divider(modifier = Modifier.padding(vertical = 10.dp))

        FilePicker(window) {
            path = it
            status = getStatus(path, fileExtension)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(textFieldValue)

        Text(
            "Status: ${status.status}",
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Button(
            onClick = {
                status = States.BUSY
                if (!checkFileExtension(path, fileExtension)) throw IllegalFileException("Wrong file selected!")
                fetchData(path, { v -> textFieldValue = v }) {
                    textFieldValue = ""
                    status = States.DONE
                }
            },
            enabled = path.isNotEmpty() && status != States.BUSY,
            modifier = Modifier.width(250.dp)
        ) {
            Text("Import starten")
        }

        Button(onClick = onDismiss, enabled = status != States.BUSY, modifier = Modifier.width(250.dp)) {
            Text("Zurück")
        }

    }
}
