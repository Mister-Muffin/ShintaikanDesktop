import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.FrameWindowScope
import models.Member
import models.Teilnahme
import java.awt.FileDialog
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.notExists
import kotlin.random.Random
import kotlin.reflect.KClass

/**
 * Creates the initial configuration file for this application is case it does not exist.
 **/
fun createConfigFile() {
    if (Path.of(configFilePath + configFileName).notExists()) {
        // Create root folder
        Path.of(configFilePath).createDirectories()
        // Create folder for upcomming database backups
        Path.of(configFilePath + "backups/").createDirectory()
        // Copy sample config to config location
        Path.of("src/main/resources/config.sample.toml")
            .copyTo(Path.of(configFilePath + configFileName), false)
    }
}

/**
 * Zählt die Trainingseinheiten eines Mitglieds
 *
 * Standardmäßig werden alle (ohne student.total) Einheien gezählt
 *
 * @param since nur Einheiten ab dem dann gegebenen Datum
 **/
fun countId(memberId: Int, teilnahme: List<Teilnahme>, since: LocalDate = LocalDate.EPOCH): Int {
    var counter = 0
    for (day in teilnahme) {
        if (day.userIds != null && day.date > since) {
            counter += day.userIds.split(",").filter { memberId.toString() == it }.size
        }
    }
    return counter
}

fun getFirstDate(id: Int, teilnahme: List<Teilnahme>): LocalDate? {
    for (day in teilnahme) {
        if (day.userIds != null) {
            if (day.userIds.split(",").filter { id.toString() == it }.isNotEmpty()) {
                return day.date
            }
        }
    }
    return null
}

/**
 * Gibt die gesamten Trainingseinheiten einer Person zurück
 *
 * student.total sind die Trainingseinheiten, die zu den in der neuen Datenbank vorhandenen Trainingseinheiten dazu addiert werden müssen,
 * da diese Trainingseinheiten sonst nicht berücksichtigt werden würden
 */
fun getTotalTrainingSessions(member: Member, teilnahme: List<Teilnahme>): Int {
    return member.total + countId(member.id, teilnahme)
}

/**
 *https://www.babbel.com/en/magazine/how-to-say-hello-in-10-different-languages
 */
fun gretting(): String {
    val array = arrayOf(
        "Salut",
        "Hola",
        "Privet",
        "Nǐ hǎo",
        "Ciao",
        "Konnichiwa",
        "Hallo",
        "Oi",
        "Anyoung",
        "Hej",
        "Habari",
        "Hoi",
        "Yassou",
        "Cześć",
        "Halo",
        "Hai",
        "Hey",
        "Hei"
    )
    return array[Random.nextInt(0, array.size - 1)]
}

/**
 * Returns the legth of an array which is equal to Array.size - 1
 */
val <T> Array<T>.lastIndex: Int
    get() {
        return this.size - 1
    }

/**
 * Removes all 2 or more whitespaces
 *
 * Example: "This_is__a____test." -> "This_is_a_test." (underscore as demonstration for whitespaces)
 */
fun String.removeMultipleWhitespaces(): String {
    return this.replace("\\s+".toRegex(), " ")
}

/**
 * Returns the next Key / Value Pair (equivalent to adding 1 to the index of a list)
 *
 * Access returned Key / Value with .first / .second
 * @author Mr. Pine <50425705+Mr-Pine@users.noreply.github.com>
 */
fun <K, V> Map<K, V>.next(oldKey: K) =
    this.keys.let { keys -> keys.elementAt(keys.indexOf(oldKey) + 1) }.let { newKey -> newKey to this[newKey] }

/**
 * Returns the previous Key / Value Pair (equivalent to subtracting 1 to the index of a list)
 *
 * Access returned Key / Value with .first / .second
 * @author Mr. Pine <50425705+Mr-Pine@users.noreply.github.com>
 */
fun <K, V> Map<K, V>.previous(oldKey: K) =
    this.keys.let { keys -> keys.elementAt(keys.indexOf(oldKey) - 1) }.let { newKey -> newKey to this[newKey] }

/**
 * Format a LocalDate with a project default formatter
 */
fun LocalDate.format(): String {
    return DateTimeFormatter.ofPattern("dd.MM.yyyy").format(this)
}

fun getRunningJar(kclass: KClass<out FrameWindowScope>): String {
    return kclass.java.protectionDomain.codeSource.location.toURI().path
}

fun showFileDialog(window: ComposeWindow, onCompleted: (directory: String?, name: String?) -> Unit) {
    val fd = FileDialog(window, "Choose a file", FileDialog.LOAD)
    fd.file = "*.jar"
    fd.isVisible = true
    onCompleted(fd.directory, fd.file)
}
