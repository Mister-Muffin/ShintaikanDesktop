package pages

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import models.Student
import models.insertTeilnahme
import java.util.*

@Composable
fun teilnehmerSelector(students: List<Student>, changeScreen: (id: Int) -> Unit) {

    val searchQuery = remember { mutableStateOf("") }
    val handleAsExam = remember { mutableStateOf(false) }

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

    fun findMatch(s: String, strings: List<String>): Boolean {
        return strings.any { a -> s.contains(a.lowercase(Locale.getDefault())) }
    }

    fun submit(isExam: Boolean) {
        var teilnahmeString = ""
        for (student in newStudents) {
            teilnahmeString = teilnahmeString + student.id + ","
        }
        changeScreen(3)
        insertTeilnahme(teilnahmeString, isExam)
    }

    val leftLazyState = rememberLazyListState()
    val rightLazyState = rememberLazyListState()


    Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxSize()) {
        Row {
            LazyColumn(state = leftLazyState, modifier = Modifier.fillMaxHeight().width(250.dp)) {
                items((if (checked.isNotEmpty()) searchStudents.filter { s -> // filter checkboxes ->
                    findMatch(s.level, checked)
                } // <- filter checkboxes
                else searchStudents).filter { // filter again for search ->
                    arrayListOf(
                        it.prename.lowercase(Locale.getDefault()),
                        it.surname.lowercase(Locale.getDefault())
                    ).joinToString(" ") // "prename surname"
                        .contains(searchQuery.value) // <- filter again for search
                }.sortedByDescending { it.id }.sortedByDescending { it.level })
                { /* linke spalte */ student ->
                    Box(
                        modifier = Modifier
                            .width(250.dp)
                            .height(25.dp)
                            .drawWithCache {
                                val gradient = Brush.horizontalGradient(
                                    colors = listOf(
                                        boxColor(student)[0],
                                        boxColor(student)[1]
                                    ),
                                    startX = size.width / 2 - 1,
                                    endX = size.width / 2 + 1,
                                )
                                onDrawBehind {
                                    drawRect(gradient)
                                }
                            }.clickable {
                                newStudents.add(student)
                                allStudents.remove(student)
                                searchStudents.remove(student)
                                searchQuery.value = ""
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
            VerticalScrollbar(
                modifier = Modifier.fillMaxHeight(),
                adapter = rememberScrollbarAdapter(
                    scrollState = leftLazyState
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.width(250.dp).fillMaxHeight()
        ) {
            Column { // search column
                Text("Suchen:")
                TextField(searchQuery.value, onValueChange = { newVal ->
                    searchQuery.value = newVal.lowercase(Locale.getDefault())
                })
            }
            LazyColumn { // filter
                val farben = arrayOf("Weiss", "Orange", "Grün", "Blau", "Violett", "Braun", "Schwarz")
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
            Column {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(4.dp)
                        .clickable { handleAsExam.value = !handleAsExam.value }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = handleAsExam.value,
                            colors = CheckboxDefaults.colors(checkedColor = Color.Gray),
                            onCheckedChange = { handleAsExam.value = it })
                        Text(text = "Ausgewählte als Prüfung eintragen")
                    }
                }

                Button( // eingabe bestätigen
                    enabled = newStudents.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    onClick = { submit(handleAsExam.value) }) {
                    Text(
                        textAlign = TextAlign.Center,
                        text = if (newStudents.isEmpty()) "Teilnehmen aus der ersten Spalte auswählen" else "Eingabe bestätigen!"
                    )
                }
            }

        }

        Row {
            LazyColumn(state = rightLazyState, modifier = Modifier.fillMaxHeight().width(250.dp)) {
                items(newStudents) { student ->
                    Box(
                        modifier = Modifier
                            .width(250.dp)
                            .height(25.dp)
                            .drawWithCache {
                                val gradient = Brush.horizontalGradient(
                                    colors = listOf(
                                        boxColor(student)[0],
                                        boxColor(student)[1]
                                    ),
                                    startX = size.width / 2 - 1,
                                    endX = size.width / 2 + 1,
                                )
                                onDrawBehind {
                                    drawRect(gradient)
                                }
                            }
                            .clickable {
                                allStudents.add(student)
                                searchStudents.add(student)
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
            VerticalScrollbar(
                modifier = Modifier.fillMaxHeight(),
                adapter = rememberScrollbarAdapter(
                    scrollState = rightLazyState
                )
            )
        }
    }
}

private fun boxColor(student: Student): Array<Color> {
    val boxColor: Array<Color> = when {
        student.level.contains("z Kyu weiss") -> {
            arrayOf(DEGREECOLORS.WHITE.color, DEGREECOLORS.WHITE.color)
        }
        student.level.contains("9. Kyu weiss-gelb") -> {
            arrayOf(DEGREECOLORS.WHITE.color, DEGREECOLORS.YELLOW.color)
        }
        student.level.contains("9/10 Kyu  weiss-rot") -> {
            arrayOf(DEGREECOLORS.WHITE.color, DEGREECOLORS.RED.color)
        }
        student.level.contains("8. Kyu gelb") -> {
            arrayOf(DEGREECOLORS.YELLOW.color, DEGREECOLORS.YELLOW.color)
        }
        student.level.contains("7. Kyu orange") -> {
            arrayOf(DEGREECOLORS.ORANGE.color, DEGREECOLORS.ORANGE.color)
        }
        student.level.contains("7/8 Kyu gelb-orange") -> {
            arrayOf(DEGREECOLORS.YELLOW.color, DEGREECOLORS.ORANGE.color)
        }
        student.level.contains("6. Kyu grün") -> {
            arrayOf(DEGREECOLORS.GREEN.color, DEGREECOLORS.GREEN.color)
        }
        student.level.contains("6/7 Kyu orange-grün") -> {
            arrayOf(DEGREECOLORS.ORANGE.color, DEGREECOLORS.GREEN.color)
        }
        student.level.contains("5. Kyu blau") -> {
            arrayOf(DEGREECOLORS.BLUE.color, DEGREECOLORS.BLUE.color)
        }
        student.level.contains("5/6 Kyu grün-blau") -> {
            arrayOf(DEGREECOLORS.GREEN.color, DEGREECOLORS.BLUE.color)
        }
        student.level.contains("4. Kyu violett") -> {
            arrayOf(DEGREECOLORS.PURPLE.color, DEGREECOLORS.PURPLE.color)
        }
        student.level.contains(". Kyu braun") -> {
            arrayOf(DEGREECOLORS.BROWN.color, DEGREECOLORS.BROWN.color)
        }
        student.level.contains(". Dan schwarz") -> {
            arrayOf(DEGREECOLORS.BLACK.color, DEGREECOLORS.BLACK.color)
        }
        else -> {
            arrayOf(Color.White, Color.White)
        }
    }
    return boxColor
}

enum class DEGREECOLORS(val color: Color) {
    WHITE(Color.White),
    YELLOW(Color(0xffffff35)),
    RED(Color(0xffff0004)),
    ORANGE(Color(0xffffaa00)),
    GREEN(Color(0xff00aa00)),
    BLUE(Color(0xff0055ff)),
    PURPLE(Color(0xff5500ff)),
    BROWN(Color(0xffaa5500)),
    BLACK(Color.Black)
}