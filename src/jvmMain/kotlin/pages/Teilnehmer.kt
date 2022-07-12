import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import models.Student

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TeilnehmerSelector(students: List<Student>, changeScreen: (id: Int) -> Unit) {

    val searchQuery = remember { mutableStateOf("") }

    val newStudents = remember { mutableStateListOf<Student>() }
    val allStudents = remember { mutableStateListOf<Student>() }
    val searchStudents = remember { mutableStateListOf<Student>() }
    remember {
        for (student in students) {
            allStudents.add(student)
            searchStudents.add(student)
        }
    }

    val checked = remember { mutableStateListOf<String>() }

    Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.width(250.dp)
        ) {
            LazyColumn {
                items(searchStudents) { student ->
                    Box(
                        modifier = Modifier.width(250.dp).height(25.dp).background(boxColor(student)).clickable {
                            newStudents.add(student)
                            allStudents.remove(student)
                            searchStudents.remove(student)
                        },
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.W500,
                            color = if (
                                student.level.contains("5. Kyu blau") ||
                                student.level.contains("4. Kyu violett") ||
                                student.level.contains(". Kyu braun") ||
                                student.level.contains(". Dan schwarz")
                            ) Color.White
                            else Color.Black,
                            modifier = Modifier.padding(start = 8.dp),
                            text = "${student.prename} ${student.surname}"
                        )
                    }
                    Divider(modifier = Modifier.width(250.dp))
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.width(250.dp).fillMaxHeight()
        ) {
            Column {
                Text("Suchen:")
                TextField(searchQuery.value, onValueChange = { newVal ->
                    searchQuery.value = newVal
                    searchStudents.clear()
                    for (student in allStudents) {
                        searchStudents.add(student)
                    }
                    if (newVal.isNotEmpty()) {
                        val filtered = searchStudents.filter {
                            it.prename.toLowerCase().contains(newVal.toLowerCase()) ||
                                    it.surname.toLowerCase().contains(newVal.toLowerCase())
                        }
                        searchStudents.clear()
                        for (student in filtered) {
                            searchStudents.add(student)
                        }
                    }
                })
            }
            LazyColumn {
                val farben = arrayOf("Weiß", "Gelb", "Rot", "Orange", "Grün", "Blau", "Violett", "Braun", "Schwarz")
                items(farben) { c ->

                    fun handleChecked() {
                        //if (farbe.value == c) farbe.value = "" else farbe.value = c
                        if (checked.contains(c)) checked.remove(c) else checked.add(c)
                    }

                    Box(
                        modifier = Modifier.width(200.dp)
                            .clickable { handleChecked() }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checked.contains(c),
                                colors = CheckboxDefaults.colors(checkedColor = Color.Gray),
                                onCheckedChange = { handleChecked() })
                            Text(text = c)
                        }
                    }
                }
            }

            Button(
                enabled = newStudents.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(60.dp),
                onClick = {}) {
                Text(
                    textAlign = TextAlign.Center,
                    text = if (newStudents.isEmpty()) "Teilnehmen aus der ersten Spalte auswählen" else "Eingabe bestätigen!"
                )
            }

        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.width(250.dp)
        ) {
            LazyColumn {
                items(newStudents) { student ->
                    Box(
                        modifier = Modifier.width(250.dp).height(25.dp).background(boxColor(student)).clickable {
                            allStudents.add(student)
                            allStudents.sortedByDescending { it.level }
                            searchStudents.add(student)
                            searchStudents.sortedByDescending { it.level }
                            newStudents.remove(student)
                        },
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.W500,
                            color = if (
                                student.level.contains("5. Kyu blau") ||
                                student.level.contains("4. Kyu violett") ||
                                student.level.contains(". Kyu braun") ||
                                student.level.contains(". Dan schwarz")
                            ) Color.White
                            else Color.Black,
                            modifier = Modifier.padding(start = 8.dp),
                            text = "${student.prename} ${student.surname}"
                        )
                    }
                    Divider(modifier = Modifier.width(250.dp))
                }
            }
        }
    }
}

@Composable
private fun boxColor(student: Student): Color {
    val boxColor: Color = when {
        student.level.contains("z Kyu weiss") -> {
            Color.White
        }
        student.level.contains("9. Kyu weiss-gelb") -> {
            Color(0xffffacf8)
        }
        student.level.contains("9/10 Kyu  weiss-rot") -> {
            Color(0xffffacf8)
        }
        student.level.contains("8. Kyu gelb") -> {
            Color(0xffffff35)
        }
        student.level.contains("7. Kyu orange") -> {
            Color(0xffffaa00)
        }
        student.level.contains("7/8 Kyu gelb-orange") -> {
            Color(0xffffacf8)
        }
        student.level.contains("6. Kyu grün") -> {
            Color(0xff00aa00)
        }
        student.level.contains("6/7 Kyu orange-grün") -> {
            Color(0xffffacf8)
        }
        student.level.contains("5. Kyu blau") -> {
            Color(0xff0055ff)
        }
        student.level.contains("5/6 Kyu grün-blau") -> {
            Color(0xffffacf8)
        }
        student.level.contains("4. Kyu violett") -> {
            Color(0xff5500ff)
        }
        student.level.contains(". Kyu braun") -> {
            Color(0xffaa5500)
        }
        student.level.contains(". Dan schwarz") -> {
            Color.Black
        }
        else -> {
            Color.White
        }
    }
    return boxColor
}