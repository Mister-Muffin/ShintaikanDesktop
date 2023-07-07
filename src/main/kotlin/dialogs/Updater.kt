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
import showFileDialog
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.name
import kotlin.reflect.KClass
import kotlin.system.exitProcess

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpdaterDialog(window: ComposeWindow, kclass: KClass<out FrameWindowScope>, onDismiss: () -> Unit) {

    var status by remember { mutableStateOf(States.NOF) }

    var path by remember { mutableStateOf("") }

    val updateDone = status == States.DONE

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
                                status = if (!name.endsWith(".jar")) States.IFE
                                else States.RDY
                            } else {
                                path = ""
                                status = States.NOF
                            }
                        }
                    })
            }
        )

        Text(
            "Status: ${status.s}",
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Button(
            onClick = {
                if (updateDone) exitProcess(0)

                upgrade(path, kclass)
                path = ""
                status = States.DONE
            },
            enabled = path.isNotEmpty() || updateDone,
            modifier = Modifier.width(250.dp)
        ) {
            Text(if (updateDone) "Program beenden" else "Aktualisieren!")
        }

        Button(onClick = onDismiss, enabled = !updateDone, modifier = Modifier.width(250.dp)) {
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

enum class States(val s: String) {
    NOF("Keine Datei ausgewählt"),
    RDY("Bereit"),
    IFE("Falsche Datei!"),
    DONE("Fertig")
}

class IllegalFileException(s: String) : Throwable(s)
