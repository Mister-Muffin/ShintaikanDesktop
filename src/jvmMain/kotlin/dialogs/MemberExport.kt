package dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import countId
import getFirstDate
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
        Dialog(
            state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 900.dp, height = 600.dp),
            title = "Teilnahme",
            onCloseRequest = onDismiss,
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight(.9f).fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    items(members) { member ->
                        Row {
                            nameText(member)
                            oldLevel(member)
                            newLevel(member)
                            unitsSinceLastExam(member, teilnahme)
                            periodLastExam(member)
                            reasonText(member, teilnahme)
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
private fun nameText(member: Student) {
    Text("${member.prename} ${member.surname}", modifier = Modifier.width(150.dp))
}

@Composable
private fun oldLevel(member: Student) {
    Text(member.level, modifier = Modifier.width(180.dp))
}

@Composable
private fun newLevel(member: Student) {
    Text(levels[levels.indexOf(member.level) + 1], modifier = Modifier.width(180.dp))
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
private fun reasonText(member: Student, teilnahme: List<Teilnahme>) {
    Text(isReadyForExam(member, teilnahme) ?: "", modifier = Modifier.width(300.dp))
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
        dateLastExam = getFirstDate(member.id, teilnahme)
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

    // ist das Mitglied in einem Zwischengrad?
    if (member.level.contains("-")) {
        when (member.level) {
            levels[1] -> {
                if (unitsSinceLastExam < 10)
                    return "Zu wenig Trainingseinheiten ($unitsSinceLastExam)"
                if (monthsSinceLastExam < 3)
                    return "Zu wenig Zeit zur letzten Prüfung vergangen ($monthsSinceLastExam monate)"
            }
            levels[2] -> {
                if (unitsSinceLastExam < 10)
                    return "Zu wenig Trainingseinheiten ($unitsSinceLastExam)"
            }
            levels[3] -> {
                if (unitsSinceLastExam < 10)
                    return "Zu wenig Trainingseinheiten ($unitsSinceLastExam)"
            }
            levels[5] -> {
                if (unitsSinceLastExam < 15)
                    return "Zu wenig Trainingseinheiten ($unitsSinceLastExam)"
            }
            levels[7] -> {
                if (unitsSinceLastExam < 22)
                    return "Zu wenig Trainingseinheiten ($unitsSinceLastExam)"
            }
            levels[9] -> {
                if (unitsSinceLastExam < 22)
                    return "Zu wenig Trainingseinheiten ($unitsSinceLastExam)"
            }
        }
    } else {
        when (member.level) {
            levels[0] -> {
                return null
            }
            levels[4] -> { // gelb
                if (unitsSinceLastExam < 10)
                    return "Zu wenig Trainingseinheiten ($unitsSinceLastExam)"
                if (monthsSinceLastExam < 3)
                    return "Zu wenig Zeit zur letzten Prüfung vergangen ($monthsSinceLastExam monate)"
                if (memberAge < 10)
                    return "Zu jung ($memberAge Jahre)"
            }
            levels[6] -> { // orange
                if (unitsSinceLastExam < 20)
                    return "Zu wenig Trainingseinheiten ($unitsSinceLastExam)"
                if (monthsSinceLastExam < 4)
                    return "Zu wenig Zeit zur letzten Prüfung vergangen ($monthsSinceLastExam monate)"
                if (memberAge < 9)
                    return "Zu jung ($memberAge Jahre)"
            }
            levels[8] -> { // grün
                if (unitsSinceLastExam < 30)
                    return "Zu wenig Trainingseinheiten ($unitsSinceLastExam)"
                if (monthsSinceLastExam < 5)
                    return "Zu wenig Zeit zur letzten Prüfung vergangen ($monthsSinceLastExam monate)"
                if (memberAge < 11)
                    return "Zu jung ($memberAge Jahre)"
            }
            levels[10] -> { // blau
                if (unitsSinceLastExam < 30)
                    return "Zu wenig Trainingseinheiten ($unitsSinceLastExam)"
                if (monthsSinceLastExam < 5)
                    return "Zu wenig Zeit zur letzten Prüfung vergangen ($monthsSinceLastExam monate)"
                if (memberAge < 13)
                    return "Zu jung ($memberAge Jahre)"
            }
            levels[11] -> { // violett
                if (unitsSinceLastExam < 45)
                    return "Zu wenig Trainingseinheiten ($unitsSinceLastExam)"
                if (monthsSinceLastExam < 8)
                    return "Zu wenig Zeit zur letzten Prüfung vergangen ($monthsSinceLastExam monate)"
                if (memberAge < 14)
                    return "Zu jung ($memberAge Jahre)"
            }
            levels[12] -> {
                if (unitsSinceLastExam < 45)
                    return "Zu wenig Trainingseinheiten ($unitsSinceLastExam)"
                if (monthsSinceLastExam < 8)
                    return "Zu wenig Zeit zur letzten Prüfung vergangen ($monthsSinceLastExam monate)"
                if (memberAge < 15)
                    return "Zu jung ($memberAge Jahre)"
            }
            levels[13] -> {
                if (unitsSinceLastExam < 45)
                    return "Zu wenig Trainingseinheiten ($unitsSinceLastExam)"
                if (monthsSinceLastExam < 8)
                    return "Zu wenig Zeit zur letzten Prüfung vergangen ($monthsSinceLastExam monate)"
                if (memberAge < 16)
                    return "Zu jung ($memberAge Jahre)"
            }
            levels[14] -> {
                if (unitsSinceLastExam < 60)
                    return "Zu wenig Trainingseinheiten ($unitsSinceLastExam)"
                if (monthsSinceLastExam < 9)
                    return "Zu wenig Zeit zur letzten Prüfung vergangen ($monthsSinceLastExam monate)"
                if (memberAge < 17)
                    return "Zu jung ($memberAge Jahre)"
            }
            levels[15] -> {
                if (unitsSinceLastExam < 60)
                    return "Zu wenig Trainingseinheiten ($unitsSinceLastExam)"
                if (monthsSinceLastExam < 10)
                    return "Zu wenig Zeit zur letzten Prüfung vergangen ($monthsSinceLastExam monate)"
                if (memberAge < 18)
                    return "Zu jung ($memberAge Jahre)"
            }
        }
    }

    return null
}