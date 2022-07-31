import androidx.compose.foundation.clickable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import dialogs.datenHolenWindow
import dialogs.examsDialog
import dialogs.manageTrainerDialog
import models.Trainer
import models.loadMessages
import models.loadStudents
import org.jetbrains.exposed.sql.Database
import pages.startPage
import pages.successPage
import pages.teilnehmerSelector
import pages.trainerSelector

//Global consts
val stickerUnits = arrayOf(0, 25, 50, 75, 100, 150, 200, 300, 500, 800)
val stickerUnitNames =
    arrayOf("", "Schlange", "Tiger", "Rabe", "Drache", "Adler", "Fuchs", "Phoenix", "Gottesanbeterin", "Reier")

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

        val students = loadStudents()
        val messages = loadMessages()

        val imageBitmap = remember { useResource("pelli2.jpg") { loadImageBitmap(it) } }

        var showDatenHolenDialog by remember { mutableStateOf(false) }
        var showExamsDialog by remember { mutableStateOf(false) }
        var showManageTrainerDialog by remember { mutableStateOf(false) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Teilnahme",
            icon = BitmapPainter(image = imageBitmap),
            state = rememberWindowState(position = WindowPosition(Alignment.Center), width = 1152.dp, height = 864.dp),
        ) {
            var screenID by remember { mutableStateOf(0) }
            var activeTrainer: Trainer? by remember { mutableStateOf(null) }

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
                    Item("Trainer verwalten", onClick = { showManageTrainerDialog = true })
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

            if (showManageTrainerDialog) {
                manageTrainerDialog(students) { showManageTrainerDialog = false }
            }
            if (showExamsDialog) {
                examsDialog(students, onDismiss = { showExamsDialog = false })
            }
            if (showDatenHolenDialog) datenHolenWindow { showDatenHolenDialog = false }

            MaterialTheme(
                typography = Typography(
                    h1 = TextStyle(
                        color = Color(0xffff8f06), fontSize = 40.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Monospace
                    ),
                    subtitle1 = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    body1 = TextStyle(fontSize = 20.sp) // All 'Text' use this as default as it seems
                )
            ) {
                when (screenID) {
                    0 -> {
                        startPage() { screenID = it }
                    }
                    1 -> {
                        trainerSelector { id, selectedTrainer -> screenID = id; activeTrainer = selectedTrainer }
                    }
                    2 -> {
                        teilnehmerSelector(students, activeTrainer!!) { screenID = it }
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