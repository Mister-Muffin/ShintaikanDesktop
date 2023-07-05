import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import dialogs.*
import models.Trainer
import org.jetbrains.exposed.sql.Database
import pages.startPage
import pages.successPage
import pages.teilnehmerSelector
import pages.trainerSelector
import java.nio.file.Path
import kotlin.system.exitProcess

const val configFileName = "config.toml"
val configFilePath = System.getProperty("user.home") + "/.local/share/shintaikan-desktop/"

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    // create file/directories in case the config file does not exist
    createConfigFile()

    // Create a TOML mapper without any custom configuration
    val mapper = tomlMapper { }

    // Read our config from file
    val tomlFile = Path.of(configFilePath + configFileName)
    val config = mapper.decode<Config>(tomlFile)
    //println(config.settings)

    val ip: String = config.settings.ip
    val port: String = config.settings.port
    val user: String = config.settings.user
    val dbPassword: String = config.settings.password
    val database: String = config.settings.database
    val appPassword: String = config.settings.appPassword
    val drivePath: String = config.settings.exportPath

    Database.connect(
        "jdbc:postgresql://${ip}:${port}/${database}",
        driver = "org.postgresql.Driver",
        user = user,
        password = dbPassword
    )

    val scope = rememberCoroutineScope()
    val viewModel = remember { ViewModel(scope) }
    viewModel.loadAll()

    val imageBitmap = remember { useResource("pelli2.jpg") { loadImageBitmap(it) } }

    var forwardedScreenId = 0

    Window(
        onCloseRequest = ::exitApplication,
        title = "Teilnahme",
        icon = BitmapPainter(image = imageBitmap),
        state = rememberWindowState(placement = WindowPlacement.Maximized),
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
                Item("Trainer verwalten", onClick = { screenID = 4; forwardedScreenId = 5 })
                Item("Daten holen", onClick = { screenID = 4; forwardedScreenId = 7 })
                Item("Hilfe/Info", onClick = { screenID = 9 })
            }
            Menu("Mitglieder", mnemonic = 'P') {
                Item("Daten abfragen", onClick = { screenID = 6 })
                Item("Daten exportieren", onClick = { screenID = 8 })
            }
        }

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
                body1 = TextStyle(fontSize = 18.sp) // All 'Text' use this as default as it seems
            ),
            shapes = Shapes(RoundedCornerShape(0.dp)),
            colors = lightColors(
                primary = Color(0xFF212121)
            )
        ) {
            // if (screenID == 0) students = loadMembers() // Reload database when moving to home screen
            when (screenID) {
                0 -> startPage(
                    viewModel.allMembers,
                    viewModel.allMessages,
                    viewModel.birthdays,
                    viewModel::reloadMessages,
                    viewModel::submitNewMessage
                ) { screenID = it }

                1 -> trainerSelector(viewModel.trainers) { id, selectedTrainer ->
                    screenID = id; activeTrainer = selectedTrainer
                }

                2 -> teilnehmerSelector(
                    viewModel.allMembers,
                    viewModel.teilnahme,
                    activeTrainer!!,
                    appPassword
                ) { screenID = it }

                3 -> successPage { screenID = it }
                // needed because dialog windows don't work on Raspberry Pi
                4 -> passwordPrompt(password = appPassword) { if (it) screenID = forwardedScreenId }

                5 -> manageTrainerDialog(viewModel.allMembers, onDismiss = { screenID = 0 })

                6 -> examsDialog(viewModel.allMembers, onDismiss = { screenID = 0 })

                7 -> DatenHolenWindow(drivePath) { exitProcess(0) }

                8 -> memberExportDialog(drivePath) { screenID = 0 }

                9 -> helpDialog(drivePath) { screenID = 0 }
                //
                else -> Text(
                    "Missing page, click the screen to go back",
                    modifier = Modifier.clickable { screenID = 0 })
            }
        }
    }
}
