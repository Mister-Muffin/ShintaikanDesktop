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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import countId
import getFirstDate
import getTotalTrainingSessions
import levels
import models.Student
import models.Teilnahme
import models.loadStudents
import models.loadTeilnahme
import java.time.LocalDate
import java.time.Period

@Composable
fun memberExportDialog(
    onDismiss: () -> Unit
) {
    val members = loadStudents()
    val teilnahme = loadTeilnahme()
//name | bish grad| next grad| training seit letzt brüf| dauer seit letzter prüf | warum kann keine prüfunge machen
    MaterialTheme {

        var searchFieldValue by remember { mutableStateOf("") }

        Dialog(
            state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 900.dp, height = 600.dp),
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
                    placeholder = { Text("Hier suchen...") },
                    singleLine = true,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
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
                            nameText(member, isReadyString)
                            oldLevel(member)
                            newLevel(member)
                            unitsSinceLastExam(member, teilnahme)
                            periodLastExam(member)
                            reasonText(isReadyString)
                        }
                    }
                }
                Button(modifier = Modifier.fillMaxWidth(.5f), onClick = { }) {
                    Text("Exportieren")
                }
            }
        }

    }
}

//<editor-fold desc="'Table' fields (textComposables)">
@Composable
private fun nameText(member: Student, isReadyString: String?) {
    Text(
        "${member.prename} ${member.surname}",
        color = if (isReadyString == null) Color.Unspecified else Color.Red,
        modifier = Modifier.width(150.dp)
    )
}

@Composable
private fun oldLevel(member: Student) {
    Text(member.level, modifier = Modifier.width(180.dp))
}

@Composable
private fun newLevel(member: Student) {
    Text(levels.lowerKey(member.level) ?: member.level, modifier = Modifier.width(180.dp))
}

@Composable
private fun unitsSinceLastExam(member: Student, teilnahme: List<Teilnahme>) {
    Text(
        if (member.date_last_exam == null) "" else "${
            countId(
                member.id,
                teilnahme,
                member.date_last_exam
            )
        }",
        modifier = Modifier.width(30.dp)
    )
}


@Composable
private fun periodLastExam(member: Student) {
    if (member.date_last_exam == null) {
        Text("", modifier = Modifier.width(30.dp))
    } else {
        val period = Period.between(member.date_last_exam, LocalDate.now())
        Text("${period.toTotalMonths()},${period.days}", modifier = Modifier.width(30.dp))
    }
}

@Composable
private fun reasonText(isReadyString: String?) {
    Text(isReadyString ?: "", modifier = Modifier.width(300.dp))
}
//</editor-fold>

/**
 * @return null if the given member can make the next exam OR:
 *
 * a string with the reason why the given member can't make the next exam
 *
 * @param member member to check
 */
private fun isReadyForExam(member: Student, teilnahme: List<Teilnahme>): String? {
    var dateLastExam: LocalDate?
    if (member.date_last_exam == null) { // set date last exam to first traing unit
        val totalTrainingSessions = getTotalTrainingSessions(member, teilnahme)
        dateLastExam = if (totalTrainingSessions == 0) null else getFirstDate(member.id, teilnahme)
        if (dateLastExam == null) {
            return "Der Schüler war noch nie im Training"
        } else {
            dateLastExam = getFirstDate(member.id, teilnahme)
        }
    } else {
        dateLastExam = member.date_last_exam
    }

    val unitsSinceLastExam = countId(member.id, teilnahme, dateLastExam!!)
    val monthsSinceLastExam = Period.between(dateLastExam, LocalDate.now()).toTotalMonths()
    val memberAge = Period.between(member.birthday, LocalDate.now().plusMonths(2)).years

    for (level in levels) {
        if (unitsSinceLastExam < level.value.units)
            return "Zu wenig Trainingseinheiten ($unitsSinceLastExam)"
        if (monthsSinceLastExam < level.value.months)
            return "Zu wenig Zeit zur letzten Prüfung vergange  n ($monthsSinceLastExam monate)"
        if (memberAge < level.value.age)
            return "Zu jung ($memberAge Jahre)"
    }
    return null
}