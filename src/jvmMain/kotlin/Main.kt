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
import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import dialogs.datenHolenWindow
import dialogs.examsDialog
import dialogs.manageTrainerDialog
import dialogs.memberExportDialog
import models.Trainer
import models.loadMessages
import models.loadStudents
import org.jetbrains.exposed.sql.Database
import pages.*
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.notExists

const val configFileName = "config.toml"

internal val configFilePath = System.getProperty("user.home") + "/.local/share/shintaikan-desktop/"

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    application {
        // create file/directories in case the config file does not exist
        if (Path.of(configFilePath + configFileName).notExists()) {
            //Create root folder
            Path.of(configFilePath).createDirectories()
            //Create folder for upcomming database backups
            Path.of(configFilePath + "backups/").createDirectory()
            // copy sample config to config location
            Path.of("src/jvmMain/resources/config.sample.toml")
                .copyTo(Path.of(configFilePath + configFileName), false)
        }

        // Create a TOML mapper without any custom configuration
        val mapper = tomlMapper { }

        // Read our config from file
        val tomlFile = Path.of(configFilePath + configFileName)
        val config = mapper.decode<Config>(tomlFile)
        //println(config.settings)

        val ip: String = config.settings.ip //System.getenv("S_DSK_IP") ?: "172.17.0.1"
        val port: String = config.settings.port //System.getenv("S_DSK_PORT") ?: "5434"
        val user: String = config.settings.user //System.getenv("S_DSK_USER") ?: "postgres"
        val password: String = config.settings.password //System.getenv("S_DSK_PASSWORD") ?: "mysecretpassword"

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
        var showDeleteMessageDialog by remember { mutableStateOf(false) }
        var showMemberExportDialog by remember { mutableStateOf(false) }

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
                    Item("Daten holen", onClick = { showDatenHolenDialog = true })
                }
                Menu("Kurznachichten", enabled = false, mnemonic = 'K') {
                    Item("Kurznachicht schreiben", onClick = { }, mnemonic = 'S')
                    Item("Kurznachicht löschen", onClick = { showDeleteMessageDialog = true }, mnemonic = 'L')
                }
                Menu("Mitglieder", mnemonic = 'P') {
                    Item("Daten abfragen", onClick = { showExamsDialog = true })
                    Item("Daten exportieren", onClick = { showMemberExportDialog = true })
                }
            }

            //region Dialog
            if (showManageTrainerDialog) {
                manageTrainerDialog(students) { showManageTrainerDialog = false }
            }
            if (showExamsDialog) {
                examsDialog(students, onDismiss = { showExamsDialog = false })
            }
            if (showDatenHolenDialog) datenHolenWindow { showDatenHolenDialog = false }
            if (showDeleteMessageDialog) deleteDialog(messages) { showDeleteMessageDialog = false }
            if (showMemberExportDialog) memberExportDialog { showMemberExportDialog = false }
            //endregion

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
                    body1 = TextStyle(fontSize = 24.sp) // All 'Text' use this as default as it seems
                )
            ) {
                when (screenID) {
                    0 -> {
                        startPage { screenID = it }
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
