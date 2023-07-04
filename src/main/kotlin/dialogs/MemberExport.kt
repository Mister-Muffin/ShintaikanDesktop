package dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Divider
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
import countId
import getFirstDate
import getTotalTrainingSessions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import levels
import models.*
import next
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.Period

private val nameWidth = 200.dp
private val levelWidth = 200.dp
private val unitsWidth = 90.dp
private val monthWidth = 90.dp
private val readyWidth = 500.dp

@Composable
fun memberExportDialog(drivePath: String, onDismiss: () -> Unit) {
    val members = remember { mutableStateListOf<Member>() }

    LaunchedEffect(Unit) {
        members.addAll(loadMembers())
    }

    val teilnahme = loadTeilnahme()

    var searchFieldValue by remember { mutableStateOf("") }
    var showTimedSuccessDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    //if (showTimedSuccessDialog) timedSuccessDialog()

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
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row {
                Text("Name", modifier = Modifier.width(nameWidth))
                Text("Alter Grad", modifier = Modifier.width(levelWidth))
                Text("Neuer Grad", modifier = Modifier.width(levelWidth))
                Text("Einh.", modifier = Modifier.width(unitsWidth))
                Text("Mon.", modifier = Modifier.width(monthWidth))
                Text("Bereit?", modifier = Modifier.width(readyWidth))
            }
            Divider(
                modifier = Modifier.width(
                    (nameWidth.value + (2 * levelWidth.value) + unitsWidth.value + monthWidth.value + readyWidth.value).dp
                )
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight(.9f).fillMaxWidth().padding(bottom = 8.dp)
        ) {
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
        Button(modifier = Modifier.fillMaxWidth(.5f), onClick = {
            coroutineScope.launch { exportMembers(drivePath) }.invokeOnCompletion {
                /* commented out because dialogs don't work on Raspberry Pi (yet?)
                coroutineScope.launch {
                    showTimedSuccessDialog = true
                    delay(2000)
                    showTimedSuccessDialog = false
                }*/
                showTimedSuccessDialog = true
            }
        }) {
            Text(if (showTimedSuccessDialog) "Erfolgreich exportiert" else "Exportieren")
        }
    }
}

//<editor-fold desc="'Table' fields (textComposables)">
@Composable
private fun nameText(member: Member, isReadyString: String?) {
    Text(
        "${member.prename} ${member.surname}",
        color = if (isReadyString == null) Color.Unspecified else Color.Red,
        modifier = Modifier.width(nameWidth)
    )
}

@Composable
private fun oldLevel(member: Member) {
    var level: String = member.level
    if (member.level.contains("Dan")) {
        level = level.drop(2) // drop the first two letters
    } else if (member.level == "z Kyu weiss") {
        level = level.drop(2)
    }
    Text(level, modifier = Modifier.width(levelWidth))
}

@Composable
private fun newLevel(member: Member) {
    Text(levels.next(member.level).first, modifier = Modifier.width(levelWidth))
}

@Composable
private fun unitsSinceLastExam(member: Member, teilnahme: List<Teilnahme>) {
    Text(
        if (member.date_last_exam == null) "" else "${
            countId(
                member,
                teilnahme,
                member.date_last_exam
            )
        }",
        modifier = Modifier.width(unitsWidth), textAlign = TextAlign.Center
    )
}


@Composable
private fun periodLastExam(member: Member) {
    if (member.date_last_exam == null) {
        Text("", modifier = Modifier.width(monthWidth))
    } else {
        val period = Period.between(member.date_last_exam, LocalDate.now())
        Text("${period.toTotalMonths()},${period.days}", textAlign = TextAlign.Center, modifier = Modifier.width(90.dp))
    }
}

@Composable
private fun reasonText(isReadyString: String?) {
    Text(isReadyString ?: "Kann nächste Prüfung machen!", modifier = Modifier.width(readyWidth))
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
fun isReadyForExam(member: Member, teilnahme: List<Teilnahme>): Pair<String, String?> {
    val dateLastExam: LocalDate = getLastExamOrFirstTrainingDate(member, teilnahme) ?: return Pair<String, String?>(
        "❌ Der Schüler war noch nie im Training",
        "Der Schüler war noch nie im Training"
    )

    val unitsSinceLastExam = countId(member, teilnahme, dateLastExam) + member.add_units_since_last_exam
    val monthsSinceLastExam = Period.between(dateLastExam, LocalDate.now()).toTotalMonths()
    // add two months to the birthday in case of holidays
    val memberAge = Period.between(member.birthday, LocalDate.now().plusMonths(2)).years

    var returnString = ""
    var returnString2 = ""

    val key = levels.next(member.level).first
    val level = levels[key] ?: throw Exception()

//    if (key == levels.next(member.level).first)
//        return Pair<String, String?>(returnString, null)

    if (unitsSinceLastExam < level.units) {
        val s = "Zu wenig Trainingseinheiten"
        val text =
            "❌ $s (braucht: ${level.units}, hat: $unitsSinceLastExam, fehlen: ${level.units - unitsSinceLastExam})"
        returnString += text
        returnString2 = s
    } else returnString += "✔️ Genug Trainingseinheiten"

    if (monthsSinceLastExam < level.months) {
        val remainingMonths = level.months - monthsSinceLastExam
        val s = "Zu wenig Zeit seit der letzten Prüfung vergangen"
        val text =
            "\n❌ $s (mind. $monthsSinceLastExam Monate) noch $remainingMonths ${if (remainingMonths == 1.toLong()) "Monat" else "Monate"}"
        returnString += text
        returnString2 = s
    } else returnString += "\n✔️ Genug Zeit seit der letzten Prüfung"

    if (memberAge < level.age) {
        val s = "Zu jung"
        val textAge = "\n❌ $s ($memberAge Jahre) ${level.age - memberAge}"
        returnString += textAge
        returnString2 = s
    } else returnString += "\n✔️ Alt genug"

    // stop the loop and return the pair when at least on condition is not satisfied (❌)
    if (returnString.contains('❌')) return Pair<String, String?>(returnString, returnString2)

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

private suspend fun exportMembers(drivePath: String) {
    val teilnahme = loadTeilnahme()
    val writer = withContext(Dispatchers.IO) {
        Files.newBufferedWriter(Paths.get("${drivePath}pruefungsabfrage.csv"))
    }

    val csvPrinter = CSVPrinter(
        writer, CSVFormat.EXCEL
        //.withHeader(StudentTable.columns)
    )

    val members = loadFullMemberTable()
    csvPrinter.printRecord("Name", "Dat. lzt. Prüf.", "Einh. s. l. Prüf.")
    members.forEach { member ->
        if (!member.is_active) return@forEach
        if (member.date_last_exam == null) return@forEach

        csvPrinter.printRecord(
            member.surname + " " + member.prename,
            member.date_last_exam,
            countId(
                member,
                teilnahme,
                member.date_last_exam
            )
        )

    }

    csvPrinter.flush()
    csvPrinter.close()
}

/*
@Composable
private fun timedSuccessDialog() {
    Dialog(
        state = rememberDialogState(
            position = WindowPosition(Alignment.Center),
            width = 300.dp,
            height = 100.dp
        ),
        title = "",
        undecorated = true,
        onCloseRequest = { }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Erfolg!")
        }
    }
}*/
