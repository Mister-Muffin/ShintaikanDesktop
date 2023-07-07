import Screen.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
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
import pages.MemberSelector
import pages.StartPage
import pages.SuccessPage
import pages.TrainerSelector
import java.nio.file.Path

const val PRODUCTION = false
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
        state = if (PRODUCTION) rememberWindowState(placement = WindowPlacement.Maximized)
        else rememberWindowState(width = 1280.dp, height = 1024.dp),
        resizable = PRODUCTION,
    ) {
        var screenID by remember { mutableStateOf(HOME) }
        var activeTrainer: Trainer? by remember { mutableStateOf(null) }

        MenuBar {
            Menu("Datei", mnemonic = 'F', enabled = !viewModel.dataLoading) {
                Item(
                    "Startseite",
                    onClick = { screenID = HOME },
                    shortcut = KeyShortcut(Key.Escape),
                    enabled = screenID != HOME
                )
                Item("Beenden", onClick = { exitApplication() }, mnemonic = 'E')
            }
            Menu("Administration", mnemonic = 'A', enabled = screenID == HOME && !viewModel.dataLoading) {
                Item("Trainer verwalten", onClick = { screenID = PASSWORD; forwardedScreenId = MANAGE_TRAINER })
                Item("Daten holen", onClick = { screenID = PASSWORD; forwardedScreenId = FETCH_DATA })
                Item("Programm aktualisieren", onClick = { screenID = PASSWORD; forwardedScreenId = UPDATER })
                Item("Hilfe/Info", onClick = { screenID = HELP })
            }
            Menu("Mitglieder", mnemonic = 'P', enabled = !viewModel.dataLoading) {
                Item("Daten abfragen", onClick = { screenID = EXAMS })
                Item("Daten exportieren", onClick = { screenID = EXPORT_MEMBERS })
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
                HOME -> StartPage(
                    viewModel.allMembers,
                    viewModel.allMessages,
                    viewModel.birthdays,
                    viewModel::reloadMessages,
                    viewModel::submitNewMessage
                ) { screenID = it }

                SELECT_TRAINER -> TrainerSelector(viewModel.trainers) { screen, selectedTrainer ->
                    screenID = screen; activeTrainer = selectedTrainer
                }

                SELECT_MEMBER -> MemberSelector(
                    viewModel.allMembers,
                    viewModel.teilnahme,
                    activeTrainer!!,
                    appPassword,
                    viewModel::insertTeilnahme
                ) { screenID = it }

                SUCCESS -> SuccessPage {
                    viewModel.loadAll()
                    screenID = it
                }
                // needed because dialog windows don't work on Raspberry Pi
                PASSWORD -> PasswordPrompt(password = appPassword) { if (it) screenID = forwardedScreenId }

                MANAGE_TRAINER -> ManageTrainerDialog(viewModel.allMembers, viewModel::reloadMembers, onDismiss = {
                    viewModel.loadAll()
                    screenID = HOME
                })

                EXAMS -> ExamsDialog(viewModel.allMembers, viewModel.teilnahme, onDismiss = { screenID = HOME })

                FETCH_DATA -> FetchDataWindow(drivePath) {
                    viewModel.loadAll()
                    screenID = HOME
                }

                EXPORT_MEMBERS -> ExportMembersDialog(viewModel.allMembers, viewModel.teilnahme, drivePath) {
                    screenID = HOME
                }

                UPDATER -> UpdaterDialog(this.window, this::class) { screenID = HOME }

                HELP -> HelpDialog(
                    drivePath,
                    this::class
                ) { screenID = HOME }
            }
        }
    }
}

enum class Screen {
    HOME, SELECT_TRAINER, MANAGE_TRAINER, SELECT_MEMBER, SUCCESS, PASSWORD, EXAMS, FETCH_DATA, EXPORT_MEMBERS, UPDATER, HELP
}
