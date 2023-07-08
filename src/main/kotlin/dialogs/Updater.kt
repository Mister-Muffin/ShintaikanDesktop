package dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import composables.FilePicker
import composables.IllegalFileException
import composables.States
import composables.getStatus
import getRunningJar
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.name
import kotlin.reflect.KClass

private const val FILE_EXTENSION = ".jar"

@Composable
fun UpdaterDialog(window: ComposeWindow, kclass: KClass<out FrameWindowScope>, onDismiss: () -> Unit) {

    var status by remember { mutableStateOf(States.NO_FILE_SELECTED) }
    var path by remember { mutableStateOf("") }


    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Programm aktualisieren", style = MaterialTheme.typography.h6)
        Divider(modifier = Modifier.padding(vertical = 10.dp))


        FilePicker(window) {
            path = it
            status = getStatus(it, FILE_EXTENSION)
        }

        Text(
            "Status: ${status.status}",
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Button(
            onClick = {
                status = States.BUSY
                if (!path.endsWith(FILE_EXTENSION)) throw IllegalFileException("Wrong file selected!")
                upgrade(path, kclass)
                status = States.DONE
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

private fun upgrade(path: String, kclass: KClass<out FrameWindowScope>) {
    val runningJar = Paths.get(getRunningJar(kclass))
    val newJarPath = Paths.get(path)

    if (!newJarPath.name.endsWith(".jar")) throw IllegalFileException("Wrong file extension!")

    Files.copy(newJarPath, runningJar, StandardCopyOption.REPLACE_EXISTING)
}
