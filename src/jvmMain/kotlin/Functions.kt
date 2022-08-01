import models.Student
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
fun countId(id: String, teilnahme: List<Teilnahme>, since: LocalDate = LocalDate.EPOCH): Int {
    var counter = 0
    for (a in teilnahme) {
        if (a.userId !== null && a.date > since) {
            counter += a.userId.split(",").filter { id == it }.size
        }
    }
    return counter
}

/**
 * Gibt die gesamten Trainingseinheiten einer Person zurück
 *
 * student.total sind die Trainingseinheiten, die zu den in der neuen Datenbank vorhandenen Trainingseinheiten dazu addiert werden müssen,
 * da diese Trainingseinheiten sonst nicht berücksichtigt werden würden
 */
fun getTotalTrainingSessions(student: Student, teilnahme: List<Teilnahme>): Int {
    return student.total + countId(student.id.toString(), teilnahme)
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
