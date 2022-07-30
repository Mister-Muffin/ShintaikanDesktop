package composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import models.Student

@Composable
fun studentList(id: Int, students: List<Student>, onClick: (nameString: String) -> Unit) {
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