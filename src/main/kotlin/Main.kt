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
import Screen.*

const val configFileName = "config.toml"
val configFilePath = System.getProperty("user.home") + "/.local/share/shintaikan-desktop/"

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    // create file/directories in case the config file does not exist
    createConfigFile()

    // Create a TOML mapper without any custom configuration
    val mapper = tomlMapper { }

    // Read config from file
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

    var forwardedScreenId = HOME

    Window(
        onCloseRequest = ::exitApplication,
        title = "Teilnahme",
        icon = BitmapPainter(image = imageBitmap),
        state = rememberWindowState(placement = WindowPlacement.Maximized),
    ) {
        var screenID by remember { mutableStateOf(HOME) }
        var activeTrainer: Trainer? by remember { mutableStateOf(null) }

        MenuBar {
            Menu("Datei", mnemonic = 'F') {
                Item(
                    "Startseite",
                    onClick = { screenID = HOME },
                    shortcut = KeyShortcut(Key.Escape),
                    enabled = screenID != HOME
                )
                Item("Beenden", onClick = { exitApplication() }, mnemonic = 'E')
            }
            Menu("Administration", mnemonic = 'A', enabled = screenID == HOME) {
                Item("Trainer verwalten", onClick = { screenID = PASSWORD; forwardedScreenId = MANAGE_TRAINER })
                Item("Daten holen", onClick = { screenID = PASSWORD; forwardedScreenId = FETCH_DATA })
                Item("Hilfe/Info", onClick = { screenID = HELP })
            }
            Menu("Mitglieder", mnemonic = 'P') {
                Item("Daten abfragen", onClick = { screenID = EXAMS })
                Item("Daten exportieren", onClick = { screenID = EXPORT_MEMBER })
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
            when (screenID) {
                HOME -> startPage(
                    viewModel.allMembers,
                    viewModel.allMessages,
                    viewModel.birthdays,
                    viewModel::reloadMessages,
                    viewModel::submitNewMessage
                ) { screenID = it }

                SELECT_TRAINER -> trainerSelector(viewModel.trainers) { screen, selectedTrainer ->
                    screenID = screen; activeTrainer = selectedTrainer
                }

                SELECT_MEMBER -> teilnehmerSelector(
                    viewModel.allMembers,
                    viewModel.teilnahme,
                    activeTrainer!!,
                    appPassword,
                    viewModel::insertTeilnahme
                ) { screenID = it }

                SUCCESS -> successPage {
                    viewModel.loadAll()
                    screenID = it
                }
                // needed because dialog windows don't work on Raspberry Pi
                PASSWORD -> passwordPrompt(password = appPassword) { if (it) screenID = forwardedScreenId }

                MANAGE_TRAINER -> manageTrainerDialog(viewModel.allMembers, viewModel::reloadMembers, onDismiss = { screenID = HOME })

                EXAMS -> examsDialog(viewModel.allMembers, viewModel.teilnahme, onDismiss = { screenID = HOME })

                FETCH_DATA -> datenHolenWindow(drivePath) {
                    viewModel.loadAll()
                    screenID = HOME
                }

                EXPORT_MEMBER -> memberExportDialog(viewModel.allMembers, viewModel.teilnahme, drivePath) { screenID = HOME }

                HELP -> helpDialog(drivePath) { screenID = HOME }
            }
        }
    }
}

enum class Screen {
    HOME, SELECT_TRAINER, MANAGE_TRAINER, SELECT_MEMBER, SUCCESS, PASSWORD, EXAMS, FETCH_DATA, EXPORT_MEMBER, HELP
}