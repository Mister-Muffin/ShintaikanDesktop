@file:Suppress("unused")

import models.Member
import models.Teilnahme
import java.time.LocalDate
import kotlin.random.Random

/**
 * Zählt die Trainingseinheiten eines Mitglieds
 *
 * Standardmäßig werden alle (ohne student.total) Einheien gezählt
 *
 * @param since nur Einheiten ab dem dann gegebenen Datum
 **/
fun countId(id: Int, teilnahme: List<Teilnahme>, since: LocalDate = LocalDate.EPOCH): Int {
    var counter = 0
    for (day in teilnahme) {
        if (day.userIds != null && day.date > since) {
            counter += day.userIds.split(",").filter { id.toString() == it }.size
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
    return member.total!! + countId(member.id, teilnahme)
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
val <T> Array<T>.length: Int
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
