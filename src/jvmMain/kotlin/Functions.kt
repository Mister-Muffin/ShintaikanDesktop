import models.Teilnahme
import java.time.LocalDate

/**
 * Zählt die Trainingseinheiten, standardmäßig alle, durch since nur Einheiten ab dem gegebenen Datum
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
