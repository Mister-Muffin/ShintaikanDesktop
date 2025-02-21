import Screen.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import composables.AppMenuBar
import dialogs.*
import kotlinx.serialization.json.Json
import model.Member
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import pages.MemberSelector
import pages.StartPage
import pages.SuccessPage
import pages.TrainerSelector
import viewmodel.ViewModel
import java.io.File
import java.nio.file.Path
import java.sql.Connection

const val configFileName = "config.toml"
val configFilePath = System.getProperty("user.home") + "/.local/share/shintaikan-desktop/"
const val dataStoreFileName = "datastore.json"
const val latestDbVersion = 2

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

    val database: String = "database.db" // config.settings.database
    val appPassword: String = config.settings.appPassword
    val drivePath: String = config.settings.exportPath

    Database.connect("jdbc:sqlite://${configFilePath}/${database}", "org.sqlite.JDBC")
    // https://jetbrains.github.io/Exposed/working-with-database.html#sqlite
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

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
        var fallBackScreenId by remember { mutableStateOf(HOME) } // fall back if passwort prompt cancelled

        var activeTrainer: Member? by remember { mutableStateOf(null) }

        AppMenuBar(
            screenID,
            { screenID = it },
            { fallBackScreenId = it },
            { forwardedScreenId = it },
            viewModel.dataLoading,
            viewModel.dbIntern.dbVersion < latestDbVersion,
            { viewModel.migrateTable(viewModel.dbIntern.dbVersion) },
            ::exitApplication
        )

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
            shapes = Shapes(RoundedCornerShape(4.dp)),
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
                    viewModel.dbIntern.dbVersion,
                    viewModel::addMessage,
                    viewModel::deleteMessage,
                    viewModel::updateMessage,
                ) { screenID = it }

                SELECT_TRAINER -> TrainerSelector(trainers) { screen, selectedTrainer ->
                    if (screen == MANAGE_TRAINER) {
                        fallBackScreenId = SELECT_TRAINER
                        forwardedScreenId = screen
                        screenID = PASSWORD
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

                SUCCESS -> SuccessPage { screenID = it }
                // needed because dialog windows don't work on Raspberry Pi
                PASSWORD -> PasswordPrompt(
                    password = appPassword,
                    cancel = { screenID = fallBackScreenId }) { if (it) screenID = forwardedScreenId }

                MANAGE_TRAINER -> ManageTrainerDialog(
                    viewModel.members,
                    viewModel::setTrainerStatus,
                    onDismiss = { screenID = HOME })

                MEMBER_STATS -> MemberStatsDialog(
                    viewModel.members,
                    viewModel.participations,
                    onDismiss = { screenID = HOME })

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
    HOME, SELECT_TRAINER, MANAGE_TRAINER, SELECT_MEMBER, SUCCESS, PASSWORD, MEMBER_STATS, FETCH_DATA, EXPORT_MEMBERS, UPDATER, HELP
}
