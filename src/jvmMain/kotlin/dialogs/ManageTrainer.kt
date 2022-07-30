package dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import models.Student
import models.editIsTrainer
import models.loadStudents

@Composable
fun manageTrainerDialog(students1: List<Student>, onDismiss: () -> Unit) {

    val students = remember { mutableStateListOf<Student>() }
    remember {
        for (student in students1) {
            students.add(student)
        }
    }

    var requirePassword by remember { mutableStateOf(true) }

    if (requirePassword) {
        passwordDialog(
            result = { pwCorrect -> requirePassword = !pwCorrect }, // if password correct, set requirePasswort to false
            onDissmiss = onDismiss
        )
    } else {
        Dialog(
            state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 750.dp, height = 600.dp),
            title = "Trainer verwalten",
            onCloseRequest = onDismiss
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Aktuelle Trainer:", style = MaterialTheme.typography.h6)
                LazyColumn {
                    items(students.filter { it.is_trainer }) { student ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(student.prename, style = MaterialTheme.typography.body1)
                            Checkbox(
                                checked = student.is_trainer,
                                onCheckedChange = {
                                    editIsTrainer(student.id, it)
                                    students.clear()
                                    for (s in loadStudents()) {
                                        students.add(s)
                                    }
                                })
                        }
                    }
                }

            }
        }
    }
}