import Screen.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
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
import kotlinx.serialization.json.Json
import model.Member
import org.jetbrains.exposed.sql.Database
import pages.MemberSelector
import pages.StartPage
import pages.SuccessPage
import pages.TrainerSelector
import viewmodel.ViewModel
import java.io.File
import java.nio.file.Path

const val configFileName = "config.toml"
val configFilePath = System.getProperty("user.home") + "/.local/share/shintaikan-desktop/"
const val dataStoreFileName = "datastore.json"

//NOTE: Archivgröße zur Vorherigen release version 28mb -> 69mb...
fun main(args: Array<String>) = application {
    var production = true
    if (args.isNotEmpty()) {
        production = args[0] != "--development"
    }

    // create file/directories in case the config file does not exist
    createConfigFile()

    // Create a TOML mapper without any custom configuration
    val mapper = tomlMapper { }
    // Read config from file
    val tomlFile = Path.of(configFilePath + configFileName)
    val config = mapper.decode<Config>(tomlFile)
    //println(config.settings)

    val datastoreFile = File(configFilePath + dataStoreFileName)
    if (!datastoreFile.exists()) {
        datastoreFile.createNewFile()
        datastoreFile.writeText("{}")
    }
    val datastoreFileText = datastoreFile.readText()
    val datastore = Json.decodeFromString<Datastore>(datastoreFileText)

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

    val imageBitmap = remember { useResource("pelli2.jpg") { loadImageBitmap(it) } }

    var forwardedScreenId = HOME

    Window(
        onCloseRequest = ::exitApplication,
        title = "Teilnahme",
        icon = BitmapPainter(image = imageBitmap),
        state = if (production) rememberWindowState(placement = WindowPlacement.Maximized)
        else rememberWindowState(width = 1280.dp, height = 1024.dp),
        resizable = production,
    ) {
        var screenID by remember { mutableStateOf(HOME) }
        var activeTrainer: Member? by remember { mutableStateOf(null) }

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
                body1 = TextStyle(fontSize = 18.sp), // All 'Text' use this as default as it seems
                caption = TextStyle(fontSize = 13.sp, color = Color(0xFF666666))
            ),
            shapes = Shapes(RoundedCornerShape(0.dp)),
            colors = lightColors(
                primary = Color(0xFF212121),
                secondary = Color(0xFF212121)
            )
        ) {
            val birthdayMembers = remember(viewModel.members) { viewModel.getBirthdayMembers() }
            val trainers = remember(viewModel.members) { viewModel.getTrainers() }

            when (screenID) {
                HOME -> StartPage(
                    viewModel.members,
                    viewModel.messages,
                    birthdayMembers,
                    datastore.lastImportPretty,
                    viewModel::addMessage,
                    viewModel::deleteMessage,
                    viewModel::updateMessage,
                    viewModel::loadTime
                ) { screenID = it }

                SELECT_TRAINER -> TrainerSelector(trainers) { screen, selectedTrainer ->
                    if (screen == MANAGE_TRAINER) {
                        screenID = PASSWORD
                        forwardedScreenId = screen
                    } else {
                        screenID = screen; activeTrainer = selectedTrainer
                    }
                }

                SELECT_MEMBER -> MemberSelector(
                    viewModel.members,
                    viewModel.participations,
                    activeTrainer!!,
                    appPassword,
                    viewModel::clearUnitsSinceLastExam,
                    viewModel::updateStickers,
                    viewModel::incrementTrainerUnits,
                    viewModel::addParticipation
                ) { screenID = it }

                SUCCESS -> SuccessPage {
                    screenID = it
                }
                // needed because dialog windows don't work on Raspberry Pi
                PASSWORD -> PasswordPrompt(password = appPassword) { if (it) screenID = forwardedScreenId }

                MANAGE_TRAINER -> ManageTrainerDialog(
                    viewModel.members,
                    viewModel::setTrainerStatus,
                    onDismiss = { screenID = HOME })

                EXAMS -> ExamsDialog(viewModel.members, viewModel.participations, onDismiss = { screenID = HOME })

                FETCH_DATA -> FetchDataWindow(this.window, viewModel::fetchData) {
                    screenID = HOME
                }

                EXPORT_MEMBERS -> ExportMembersDialog(viewModel.members, viewModel.participations, drivePath) {
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
