import androidx.compose.foundation.clickable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import models.loadMessages
import models.loadStudents
import models.loadTrainers
import org.jetbrains.exposed.sql.Database
import pages.StartPage
import pages.successPage
import pages.teilnehmerSelector
import java.io.File

fun main() {
    application {

        val ip: String = System.getenv("S_DSK_IP") ?: "172.17.0.1"
        val port: String = System.getenv("S_DSK_PORT") ?: "5434"
        val table: String = System.getenv("S_DSK_TABLE") ?: "test"
        val user: String = System.getenv("S_DSK_USER") ?: "postgres"
        val password: String = System.getenv("S_DSK_PASSWORD") ?: "mysecretpassword"

        Database.connect(
            "jdbc:postgresql://${ip}:${port}/${table}",
            driver = "org.postgresql.Driver",
            user = user,
            password = password
        )

        val trainers = loadTrainers()
        val students = loadStudents()
        val messages = loadMessages()

        val file = File("src/jvmMain/resources/pelli2.jpg")
        val imageBitmap: ImageBitmap = remember(file) {
            loadImageBitmap(file.inputStream())
        }


        Window(
            onCloseRequest = ::exitApplication,
            title = "Teilnahme",
            icon = BitmapPainter(image = imageBitmap),
            state = rememberWindowState(width = 1152.dp, height = 864.dp),
        ) {
            var screenID by remember { mutableStateOf(0) }
            MaterialTheme {
                when (screenID) {
                    0 -> {
                        StartPage(students, messages) { screenID = it }
                    }
                    1 -> {
                        TrainerSelector(trainers) { screenID = it }
                    }
                    2 -> {
                        teilnehmerSelector(students) { screenID = it }
                    }
                    3 -> {
                        successPage { screenID = it }
                    }
                    else -> Text("Missing page", modifier = Modifier.clickable { screenID = 0 })
                }
            }
        }
    }
}