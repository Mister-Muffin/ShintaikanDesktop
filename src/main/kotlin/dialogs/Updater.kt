package dialogs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import getRunningJar
import java.awt.FileDialog
import java.nio.file.Paths
import kotlin.reflect.KClass

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpdaterDialog(window: ComposeWindow, kclass: KClass<out FrameWindowScope>, onDismiss: () -> Unit) {

    var path by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("Keine Datei ausgewählt") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Programm aktualisieren", style = MaterialTheme.typography.h6)
        Divider(modifier = Modifier.padding(vertical = 10.dp))
        OutlinedTextField(
            path,
            readOnly = true,
            onValueChange = { path = it },
            trailingIcon = {
                Icon(
                    Icons.Default.Search,
                    "",
                    modifier = Modifier.onClick {
                        showFileDialog(window) { directory, name ->
                            if (!directory.isNullOrEmpty() && !name.isNullOrEmpty()) {
                                path = directory + name
                                statusMessage = if (!name.endsWith(".jar")) "Falsche Datei!"
                                else "Bereit"
                            } else {
                                path = ""
                                statusMessage = "Keine Datei ausgewählt"
                            }
                        }
                    })
            }
        )

        Text(
            "Status: $statusMessage",
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Button(
            onClick = {
                try {
                    upgrade(path, kclass)
                } catch (e: IllegalFileException) {
                    statusMessage = e.toString()
                }
                path = ""
                statusMessage = "Fertig"
            },
            enabled = path.isNotEmpty(),
            modifier = Modifier.width(250.dp)
        ) {
            Text("Aktualisieren!")
        }

        Button(onClick = onDismiss, modifier = Modifier.width(250.dp)) {
            Text("Zurück")
        }
    }

}

private fun upgrade(path: String, kclass: KClass<out FrameWindowScope>) {
    val runningJar = Paths.get(getRunningJar(kclass))
    val newJarPath = Paths.get(path)

    if (!newJarPath.endsWith(".jar")) throw IllegalFileException("Wrong file extension!")

    // Files.copy(newJarPath, runningJar, StandardCopyOption.REPLACE_EXISTING)
}

private fun showFileDialog(window: ComposeWindow, onCompleted: (directory: String?, name: String?) -> Unit) {
    val fd = FileDialog(window, "Choose a file", FileDialog.LOAD)
    fd.file = "*.jar"
    fd.isVisible = true
    onCompleted(fd.directory, fd.file)
}

class IllegalFileException(s: String) : Throwable(s)
