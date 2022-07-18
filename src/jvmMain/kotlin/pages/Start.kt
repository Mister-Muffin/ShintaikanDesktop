package pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import models.Student
import java.time.LocalDate
import java.time.Period

@Composable
fun StartPage(students: List<Student>, changeScreen: (id: Int) -> Unit) {

    val allStudents = remember { mutableStateListOf<Student>() }
    remember {
        for (student in students) {
            allStudents.add(student)
        }
    }
    val birthdays = remember { loadBirthdays(students) }

    Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                item {
                    Text(
                        "Willkommen!",
                        style = TextStyle(color = Color(0xffff8f06), fontSize = 30.sp),
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Monospace,
                    )
                }
                item {
                    Divider(
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
                items(birthdays) {
                    Row {
                        val period = Period.between(it.birthday, LocalDate.now()).days
                        Text(
                            text = it.prename + ": ",
                            //style = TextStyle(color = Color(0xffff8f06), fontSize = 30.sp),
                            fontWeight = FontWeight.Normal,
                        )
                        Text(
                            text = if (period < 0) {
                                "vor ${period * (-1)} Tagen"
                            } else if (period > 0) {
                                "in $period Tagen"
                            } else {
                                "heute"
                            },
                            //style = TextStyle(color = Color(0xffff8f06), fontSize = 30.sp),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

private fun loadBirthdays(students: List<Student>): MutableList<Student> {
    val birthdays = mutableListOf<Student>()

    for (student in students) {
        if (student.birthday == null) continue
        if (student.birthday >= (LocalDate.now().minusDays(3)) &&
            student.birthday <= LocalDate.now().plusDays(3)
        ) {
            birthdays.add(student)
        }
    }

    return birthdays
}