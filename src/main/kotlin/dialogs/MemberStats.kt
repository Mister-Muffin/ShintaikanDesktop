package dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import composables.StudentList
import countId
import getTotalTrainingSessions
import models.Member
import models.Teilnahme
import models.loadMembers
import models.loadTeilnahme
import next
import stickerUnits
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@Composable
fun examsDialog(members: List<Member>, onDismiss: () -> Unit) {

    val allMembers = remember { mutableStateListOf<Member>() }
    val searchMembers = remember { mutableStateListOf<Member>() }
    remember {
        for (student in members) {
            allMembers.add(student)
            searchMembers.add(student)
        }
    }

    val searchFieldVal = remember { mutableStateOf("") }

    val studentFilter = allMembers.filter {
        (it.prename + it.surname)
            .lowercase()
            .contains(searchFieldVal.value.lowercase().replace(" ", ""))
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mitgliedsdaten abfragen", style = MaterialTheme.typography.h6)
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        OutlinedTextField(
            value = searchFieldVal.value,
            onValueChange = { searchFieldVal.value = it },
            placeholder = {
                Text(
                    "Suchen... (mind. 3 Zeichen)",
                    style = TextStyle.Default.copy(fontSize = 16.sp)
                )
            },
            modifier = Modifier.padding(bottom = 10.dp).width(300.dp)
        )
        LazyColumn {
            if (searchFieldVal.value.length > 2) {
                if (studentFilter.size >= 2) {
                    items(allMembers.filter {
                        (it.prename + it.surname)
                            .lowercase()
                            .contains(searchFieldVal.value.lowercase().replace(" ", ""))
                    }) {
                        StudentList().studentList(
                            it.id,
                            members,
                            onClick = { nameString -> searchFieldVal.value = nameString })
                    }
                } else if (studentFilter.size == 1) {
                    item { studentStats(studentFilter[0]) }
                } else {
                    item { Text("Keine Personen gefunden") }
                }
            }
        }
    }
}

@Composable
private fun studentStats(member: Member) { //datum letzte prüfung | wie lange her y m d | einheiten seit l prüf | einheiten gesamt
    val teilnahme = loadTeilnahme()
    val students = loadMembers()
    return Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            val nameString: String = member.prename + " " + member.surname // Join pre- and surname

            Text(
                "$nameString${if (member.is_trainer) " (Trainer)" else ""}", // This adds "(Trainer)" to the name string if the member is also a trainer
                style = MaterialTheme.typography.h6,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            ) // Name of the member
        }
        //Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            "Hat am: ${
                DateTimeFormatter.ofPattern("dd.MM.yyyy").format(member.birthday)
            } Geburtstag, ist ${Period.between(member.birthday, LocalDate.now()).years} Jahre alt"
        )

        // Show member's group and replace "Benjamini" with "Karamini" if so
        Text("Gruppe: ${if (member.group == "Benjamini") "Karamini" else member.group}")

        var level: String = member.level
        if (member.level.contains("Dan")) {
            level = level.drop(2) // drop the first two letters
        } else if (member.level == "z Kyu weiss") {
            level = level.drop(2)
        }
        Text("Grad: $level")

        textTotalTrainingSessions(member, teilnahme)

        Divider()

        // Sollte die Person bereits eine Prüfung gemacht haben,
        // zeige das Datum der letzten Prüfung und bau den string für die Differenz zu diesem Datum zusammen
        if (member.date_last_exam != null) {
            textLastExam(member)
            Text(
                "Einheiten seit der letzten Prüfung: ${countId(member, teilnahme, member.date_last_exam)}"
            )

            // Zeitraum zwischen der letzten Prüfung und dem heutigen Datum
            val period = Period.between(member.date_last_exam, LocalDate.now())


            //<editor-fold desc="Date constants">
            // Zeigt die Jahre, falls diese nicht 0 sind
            val years =
                when (period.years) {
                    0 -> ""
                    1 -> period.years.toString() + " Jahr"
                    else -> period.years.toString() + " Jahren"
                }

            // Zeigt die Monate, falls diese nicht 0 sind
            val months =
                when (period.months) {
                    0 -> ""
                    1 -> period.months.toString() + " Monat"
                    else -> period.months.toString() + " Monaten"
                }

            // Zeigt die Tage, falls diese nicht 0 sind
            val days =
                when (period.days) {
                    0 -> ""
                    1 -> period.days.toString() + " Tag"
                    else -> period.days.toString() + " Tagen"
                }
            //</editor-fold>

            Text("Letzte Prüfung vor: " +
                    "${if (years.isNotEmpty()) "$years, " else ""}${months.ifEmpty { "" }}${if (days.isNotEmpty() && months.isNotEmpty()) " und " else ""}${days.ifEmpty { "" }}"
            )

        } else {
            Text("Noch keine Prüfung")
        }

        val ready = isReadyForExam(member, teilnahme)
        if (member.date_last_exam != null) Text(ready.first)
        if (ready.second == null) {
            Text("Bereit für die nächste Prüfung", color = Color.Green)
        } else {
            Text("Noch nicht bereit für die nächste Prüfung", color = Color.Red)
        }

        if (member.trainer_units != 0) Text("Hat ${member.trainer_units} mal Training gegeben")

        Divider()

        val activeStickerCount = member.sticker_recieved
        if (member.sticker_recieved == 0) {
            Text("Hat noch keinen Sticker bekommen")
        } else {
            val stickerHistoryList = member.sticker_recieved_by.toString().trim(',').split(",")
            stickerHistoryList.forEach {
                val singleStats = it.split(":")
                val stickerUnit: Int = singleStats[0].toInt()
                val stickerName = stickerUnits[stickerUnit]
                val stickerBy: Int = singleStats[1].toInt()
                val stickerByTrainer = students.filter { f -> stickerBy == f.id }[0]
                val stickerByTrainerName = "${stickerByTrainer.prename} ${stickerByTrainer.surname}"
                val stickerDate = singleStats[2]
                val stickerDateFormatted = LocalDate.parse(stickerDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                Text("Sticker $stickerName ($stickerUnit) bekommen von $stickerByTrainerName am $stickerDateFormatted")
            }
        }

        if (member.sticker_recieved == stickerUnits.keys.last())
            Text("Es gibt keinen weiteren Sticker")
        else {
            val nextStickerCount = stickerUnits.next(activeStickerCount).first
            val nextStickerName = stickerUnits[nextStickerCount]
            Text("Nächster Sticker: $nextStickerName ($nextStickerCount)")
        }
    }
}

/**
 * Text composable with displays the date of the student's total trainings sessions with some additional text
 */
@Composable
private fun textTotalTrainingSessions(member: Member, teilnahme: List<Teilnahme>) {
    Text("Alle Trainingseinheiten: " + getTotalTrainingSessions(member, teilnahme))
}

/**
 * Text composable with displays the date of the student's last exam with some additional text
 */
@Composable
private fun textLastExam(member: Member) {
    Text(
        "Letzte Prüfung am: ${
            DateTimeFormatter.ofPattern("dd.MM.yyyy").format(member.date_last_exam)
        }"
    )
}
