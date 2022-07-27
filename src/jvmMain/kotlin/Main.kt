import androidx.compose.foundation.clickable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dialogs.examsDialog
import models.loadMessages
import models.loadStudents
import models.loadTrainers
import org.jetbrains.exposed.sql.Database
import pages.startPage
import pages.successPage
import pages.teilnehmerSelector

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    application {

        val ip: String = System.getenv("S_DSK_IP") ?: "172.17.0.1"
        val port: String = System.getenv("S_DSK_PORT") ?: "5434"
        val user: String = System.getenv("S_DSK_USER") ?: "postgres"
        val password: String = System.getenv("S_DSK_PASSWORD") ?: "mysecretpassword"

        Database.connect(
            "jdbc:postgresql://${ip}:${port}/",
            driver = "org.postgresql.Driver",
            user = user,
            password = password
        )

        val trainers = loadTrainers()
        val students = loadStudents()
        val messages = loadMessages()

        val imageBitmap = remember { useResource("pelli2.jpg") { loadImageBitmap(it) } }

        var shiftPressed by remember { mutableStateOf(false) }
        var showExamsDialog by remember { mutableStateOf(false) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Teilnahme",
            icon = BitmapPainter(image = imageBitmap),
            state = rememberWindowState(width = 1152.dp, height = 864.dp),
            onKeyEvent = {
                shiftPressed = it.isShiftPressed
                shiftPressed
            }
        ) {
            var screenID by remember { mutableStateOf(0) }

            MenuBar {
                Menu("Datei", mnemonic = 'F') {
                    Item(
                        "Startseite",
                        onClick = { screenID = 0 },
                        shortcut = KeyShortcut(Key.Escape),
                        enabled = screenID != 0
                    )
                    Item("Beenden", onClick = { exitApplication() }, mnemonic = 'E')
                }
                Menu("Administration", mnemonic = 'A', enabled = screenID == 0) {
                    Item("Daten holen", onClick = { })
                }
                Menu("Kurznachichten", mnemonic = 'K') {
                    Item("Kurznachicht schreiben", onClick = { }, mnemonic = 'S')
                    Item("Kurznachicht löschen", onClick = { }, mnemonic = 'L')
                }
                Menu("Prüfungen", mnemonic = 'P') {
                    Item(
                        "Letzte Prüfungen abfragen",
                        onClick = { showExamsDialog = true },
                        mnemonic = 'K'
                    )
                }
            }

            if (showExamsDialog) {
                examsDialog(onDismiss = {
                    showExamsDialog = false
                })
            }

            MaterialTheme {
                when (screenID) {
                    0 -> {
                        startPage(students, messages, shiftPressed) { screenID = it }
                    }
                    1 -> {
                        TrainerSelector(trainers) { screenID = it }
                    }
                    2 -> {
                        teilnehmerSelector(students) { screenID = it }
                    }
                    3 -> {
                        successPage { screenID = it }
                    }
                    else -> Text("Missing page", modifier = Modifier.clickable { screenID = 0 })
                }
            }
        }
    }
}