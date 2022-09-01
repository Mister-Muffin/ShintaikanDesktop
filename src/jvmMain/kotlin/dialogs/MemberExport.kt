package dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import configFilePath
import countId
import getFirstDate
import getTotalTrainingSessions
import levels
import models.*
import next
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import windowWidth
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.Period

@Composable
fun memberExportDialog(
    onDismiss: () -> Unit
) {
    val members = loadMembers()
    val teilnahme = loadTeilnahme()
    MaterialTheme {

        var searchFieldValue by remember { mutableStateOf("") }

        Dialog(
            state = rememberDialogState(
                position = WindowPosition(Alignment.Center),
                width = windowWidth,
                height = 700.dp
            ),
            title = "Teilnahme",
            onCloseRequest = onDismiss,
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    searchFieldValue,
                    onValueChange = { searchFieldValue = it },
                    placeholder = {
                        Text("Hier suchen...", style = TextStyle.Default.copy(fontSize = 16.sp))
                    },
                    singleLine = true,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                LazyColumn(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight(.9f).fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    item {
                        Row {
                            Text("Name", modifier = Modifier.width(150.dp))
                            Text("Alter Grad", modifier = Modifier.width(180.dp))
                            Text("Neuer Grad", modifier = Modifier.width(180.dp))
                            Text("Einh.", modifier = Modifier.width(90.dp))
                            Text("Mon.", modifier = Modifier.width(90.dp))
                            Text("Bereit?", modifier = Modifier.width(350.dp))
                        }
                    }
                    items(members.filter {
                        (it.prename + it.surname).lowercase().contains(searchFieldValue.lowercase().replace(" ", ""))
                    }) { member ->
                        val isReadyString = isReadyForExam(member, teilnahme)
                        Row {
                            nameText(member, isReadyString.second)
                            oldLevel(member)
                            newLevel(member)
                            unitsSinceLastExam(member, teilnahme)
                            periodLastExam(member)
                            reasonText(isReadyString.second)
                        }
                    }
                }
                Button(modifier = Modifier.fillMaxWidth(.5f), onClick = { exportMembers() }) {
                    Text("Exportieren")
                }
            }
        }

    }
}

//<editor-fold desc="'Table' fields (textComposables)">
@Composable
private fun nameText(member: Member, isReadyString: String?) {
    Text(
        "${member.prename} ${member.surname}",
        color = if (isReadyString == null) Color.Unspecified else Color.Red,
        modifier = Modifier.width(150.dp)
    )
}

@Composable
private fun oldLevel(member: Member) {
    Text(member.level, modifier = Modifier.width(180.dp))
}

@Composable
private fun newLevel(member: Member) {
    Text(levels.next(member.level).first, modifier = Modifier.width(180.dp))
}

@Composable
private fun unitsSinceLastExam(member: Member, teilnahme: List<Teilnahme>) {
    Text(
        if (member.date_last_exam == null) "" else "${
            countId(
                member.id,
                teilnahme,
                member.date_last_exam
            )
        }",
        modifier = Modifier.width(90.dp), textAlign = TextAlign.Center
    )
}


@Composable
private fun periodLastExam(member: Member) {
    if (member.date_last_exam == null) {
        Text("", modifier = Modifier.width(90.dp))
    } else {
        val period = Period.between(member.date_last_exam, LocalDate.now())
        Text("${period.toTotalMonths()},${period.days}", textAlign = TextAlign.Center, modifier = Modifier.width(90.dp))
    }
}

@Composable
private fun reasonText(isReadyString: String?) {
    Text(isReadyString ?: "Kann nächste Prüfung machen!", modifier = Modifier.width(500.dp))
}
//</editor-fold>

/**
 * @return Pair<String, String?>
 *
 * First part contains a text with all (three) requirememnts, satisfied or not,
 * with according description
 *
 * Secord part is null if the given member can make the next exam OR:
 *
 * a string with the reason why the given member can't make the next exam
 *
 * First part is longer, like a detailed description, second part is shorter, for use in a one-liner
 *
 * @param member member to check
 * @see studentStats
 * @see memberExportDialog
 */
internal fun isReadyForExam(member: Member, teilnahme: List<Teilnahme>): Pair<String, String?> {
    val dateLastExam: LocalDate = getLastExamOrFirstTrainingDate(member, teilnahme) ?: return Pair<String, String?>(
        "❌ Der Schüler war noch nie im Training",
        "Der Schüler war noch nie im Training"
    )

    val unitsSinceLastExam = countId(member.id, teilnahme, dateLastExam)
    val monthsSinceLastExam = Period.between(dateLastExam, LocalDate.now()).toTotalMonths()
    val memberAge = Period.between(member.birthday, LocalDate.now().plusMonths(2)).years

    var returnString = ""
    var returnString2 = ""

    for (level in levels) {
        if (level.key == levels.next(member.level).first)
            return Pair<String, String?>(returnString, null)

        returnString = ""

        if (unitsSinceLastExam < level.value.units) {
            val s = "Zu wenig Trainingseinheiten"
            val text = "❌ $s ($unitsSinceLastExam) ${level.value.units - unitsSinceLastExam}"
            returnString += text
            returnString2 = s
        } else returnString += "✔️ Genug Trainingseinheiten"

        if (monthsSinceLastExam < level.value.months) {
            val remainingMonths = level.value.months - monthsSinceLastExam
            val s = "Zu wenig Zeit seit der letzten Prüfung vergangen"
            val text =
                "\n❌ $s (mind. $monthsSinceLastExam Monate) noch $remainingMonths ${if (remainingMonths == 1.toLong()) "Monat" else "Monate"}"
            returnString += text
            returnString2 = s
        } else returnString += "\n✔️ Genug Zeit seit der letzten Prüfung"

        if (memberAge < level.value.age) {
            val s = "Zu jung"
            val textAge = "\n❌ $s ($memberAge Jahre) ${level.value.age - memberAge}"
            returnString += textAge
            returnString2 = s
        } else returnString += "\n✔️ Alt genug"

        // stop the loop and return the pair when at least on condition is not satisfied (❌)
        if (returnString.contains('❌')) return Pair<String, String?>(returnString, returnString2)
    }
    //else, if all requirements are satisfied, return null as the second part (sse JavaDoc)
    return Pair<String, String?>(returnString, null)
}

/**
 * @return Date from the students last exam.
 *
 * If the student hasn't done an exam yet, the function returns the date of the
 * students first training sesstion.
 *
 * If the student wasn't in training ever, the funtion returns null
 */
fun getLastExamOrFirstTrainingDate(member: Member, teilnahme: List<Teilnahme>): LocalDate? {
    var dateLastExam: LocalDate?
    if (member.date_last_exam == null) { // set date last exam to first traing unit
        val totalTrainingSessions = getTotalTrainingSessions(member, teilnahme)
        dateLastExam = if (totalTrainingSessions == 0) null else getFirstDate(member.id, teilnahme)
        if (dateLastExam == null) {
            return null // "Der Schüler war noch nie im Training."
        } else {
            dateLastExam = getFirstDate(member.id, teilnahme)
        }
    } else {
        dateLastExam = member.date_last_exam
    }
    return dateLastExam
}

private fun exportMembers() {
    val teilnahme = loadTeilnahme()
    val writer = Files.newBufferedWriter(Paths.get("${configFilePath}member_export.csv"))

    val csvPrinter = CSVPrinter(
        writer, CSVFormat.DEFAULT
        //.withHeader(StudentTable.columns)
    )

    val members = loadFullMemberTable()

    members.forEach { member ->
        if (!member.is_active) return@forEach
        if (member.date_last_exam == null) return@forEach

        csvPrinter.printRecord(
            member.surname + " " + member.prename,
            member.date_last_exam,
            countId(
                member.id,
                teilnahme,
                member.date_last_exam
            )
        )

    }

    csvPrinter.flush()
    csvPrinter.close()
}
