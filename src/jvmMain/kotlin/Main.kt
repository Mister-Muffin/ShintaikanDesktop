import androidx.compose.foundation.clickable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import models.loadStudents
import models.loadTrainers
import org.jetbrains.exposed.sql.Database
import pages.TeilnehmerSelector

fun main() {
    application {
        Database.connect(
            "jdbc:postgresql://172.17.0.1:5434/test",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "mysecretpassword"
        )

        val trainers = loadTrainers()
        val students = loadStudents()

        Window(onCloseRequest = ::exitApplication) {
            var screenID by remember { mutableStateOf(0) }
            MaterialTheme {
                val loopId = 0
                when (screenID) {
                    0 -> {
                        TrainerSelector(trainers) { screenID = it }
                    }
                    1 -> {
                        TeilnehmerSelector(students) {
                            screenID = it
                        }
                    }
                    else -> Text("Missing page", modifier = Modifier.clickable { screenID = 0 })
                }
            }
        }
    }
}