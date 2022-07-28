package dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import models.Student
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
        title = "AAAAAAAAAAAAAAAAAAAAAAA",
        onCloseRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = searchFieldVal.value,
                onValueChange = { searchFieldVal.value = it },
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
                            studentList(it.id, students, onClick = { nameString -> searchFieldVal.value = nameString })
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
private fun studentList(id: Int, students: List<Student>, onClick: (nameString: String) -> Unit) {
    val student = students.find { it.id == id }
    return if (student != null) {
        Row(horizontalArrangement = Arrangement.Center) {
            val nameString: String = student.prename + " " + student.surname
            Text(
                text = nameString,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable { onClick(nameString) }.padding(10.dp).width(300.dp)
            )
        }
    } else {
        Row { }
    }
}

@Composable
private fun studentStats(student: Student) { //datum letzte prüfung | wie lange her y m d | einheiten seit l prüf | einheiten gesamt
    return Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val nameString: String = student.prename + " " + student.surname

        Text(nameString)

        if (student.date_last_exam !== null) Text(
            "Letzte Prüfung am: ${
                DateTimeFormatter.ofPattern("dd.MM.yyyy").format(student.date_last_exam)
            }"
        ) else Text("Noch keine Prüfung")
        if (student.date_last_exam !== null) {
            val period = Period.between(student.date_last_exam, LocalDate.now())
            val years =
                if (period.years == 0) "" else if (period.years == 1) period.years.toString() + " Jahr" else period.years.toString() + " Jahren"
            val months =
                if (period.months == 0) "" else if (period.months == 1) period.months.toString() + " Monat" else period.months.toString() + " Monaten"
            val days =
                if (period.days == 0) "" else if (period.days == 1) period.days.toString() + " Tag" else period.days.toString() + " Tagen"
            Text("Letzte Prüfung vor: ${if (years.isNotEmpty()) "$years, " else ""}${if (months.isNotEmpty()) months else ""} ${if (days.isNotEmpty()) "und $days" else ""}")
        }
    }
}