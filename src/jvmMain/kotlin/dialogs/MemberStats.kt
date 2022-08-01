package dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import composables.StudentList
import countId
import getTotalTrainingSessions
import models.Student
import models.Teilnahme
import models.loadTeilnahme
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@Composable
fun examsDialog(students: List<Student>, onDismiss: () -> Unit) {

    val allStudents = remember { mutableStateListOf<Student>() }
    val searchStudents = remember { mutableStateListOf<Student>() }
    remember {
        for (student in students) {
            allStudents.add(student)
            searchStudents.add(student)
        }
    }

    val searchFieldVal = remember { mutableStateOf("") }

    val studentFilter = allStudents.filter {
        (it.prename + it.surname)
            .lowercase()
            .contains(searchFieldVal.value.lowercase().replace(" ", ""))
    }

    Dialog(
        state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 750.dp, height = 600.dp),
        title = "Daten abfragen",
        onCloseRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Mitgliedsdaten abfragen", style = MaterialTheme.typography.h6)
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            OutlinedTextField(
                value = searchFieldVal.value,
                onValueChange = { searchFieldVal.value = it },
                placeholder = { Text("Suchen... (mind. 3 Zeichen)") },
                modifier = Modifier.padding(bottom = 10.dp).width(300.dp)
            )
            LazyColumn {
                if (searchFieldVal.value.length > 2) {
                    if (studentFilter.size >= 2) {
                        items(allStudents.filter {
                            (it.prename + it.surname)
                                .lowercase()
                                .contains(searchFieldVal.value.lowercase().replace(" ", ""))
                        }) {
                            StudentList().studentList(
                                it.id,
                                students,
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
}

@Composable
private fun studentStats(student: Student) { //datum letzte prüfung | wie lange her y m d | einheiten seit l prüf | einheiten gesamt
    val teilnahme = loadTeilnahme()
    return Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val nameString: String = student.prename + " " + student.surname

        Text(nameString)
        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Sollte die Person bereits eine Prüfung gemacht haben, zeige das Datum der letzten Prüfung und bau den string für die Differenz zu diesem Datum zusammen

        if (student.date_last_exam != null) {
            textLastExam(student)

            val period = Period.between(
                student.date_last_exam,
                LocalDate.now()
            ) // Zeitraum zwischen der letzten Prüfung und dem heutigen Datum

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

            Text("Letzte Prüfung vor: ${if (years.isNotEmpty()) "$years, " else ""}${if (months.isNotEmpty()) months else ""}${if (days.isNotEmpty() && months.isNotEmpty()) "und" else ""}${if (days.isNotEmpty()) days else ""}")

            Text(
                "Einheiten seit der letzten Prüfung: " + countId(
                    student.id.toString(),
                    teilnahme,
                    student.date_last_exam
                ).toString()
            )
        } else {
            Text("Noch keine Prüfung")
        }

        textTotalTrainingSessions(student, teilnahme)
    }
}

@Composable
private fun textTotalTrainingSessions(student: Student, teilnahme: List<Teilnahme>) {
    Text("Einheiten gesamt: " + getTotalTrainingSessions(student, teilnahme))
}

/**
 * Text composable with the date of the student's last exam
 */
@Composable
private fun textLastExam(student: Student) {
    Text(
        "Letzte Prüfung am: ${
            DateTimeFormatter.ofPattern("dd.MM.yyyy").format(student.date_last_exam)
        }"
    )
}
