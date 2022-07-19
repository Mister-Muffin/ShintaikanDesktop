package pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import models.Message
import models.Student
import java.time.LocalDate
import java.time.Period

@Composable
fun StartPage(students: List<Student>, messages: List<Message>, changeScreen: (id: Int) -> Unit) {

    val allStudents = remember { mutableStateListOf<Student>() }
    remember {
        for (student in students) {
            allStudents.add(student)
        }
    }
    val birthdays = remember { loadBirthdays(students) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(all = 8.dp)) {
        Text(
            "Willkommen!",
            style = TextStyle(color = Color(0xffff8f06), fontSize = 30.sp),
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Monospace,
        )
        Divider(
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Box() {
            buttonRow(changeScreen)
        }
        Row(
            //horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxSize().padding(top = 24.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(0.5F),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    headerText("Es haben/hatten Geburtstag:")
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
            Divider(modifier = Modifier.fillMaxHeight().width(1.dp))
            LazyColumn(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item { headerText("Kurznachrichten") }
                item {
                    OutlinedTextField(
                        value = "Testtext",
                        modifier = Modifier.fillMaxWidth(0.8F),
                        trailingIcon = {
                            IconButton(onClick = { changeScreen(1) }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        },
                        singleLine = true,
                        onValueChange = {}
                    )
                }
                items(messages) { message(it.message) }
            }
        }
    }
}

@Composable
private fun message(text: String) {
    Text(text = text)
}

@Composable
private fun headerText(text: String) {
    Text(text = text, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
}

@Composable
private fun buttonRow(changeScreen: (id: Int) -> Unit) {
    @Composable
    fun rowButton(text: String, onClick: () -> Unit, enabled: Boolean) {
        Button(
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray, contentColor = Color.White),
            //modifier = Modifier.width(150.dp).height(40.dp),
            onClick = onClick
        ) {
            Text(text)
        }
    }

    Row {
        rowButton(text = "Teilnehmer eintragen", enabled = true, onClick = { changeScreen(1) })
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