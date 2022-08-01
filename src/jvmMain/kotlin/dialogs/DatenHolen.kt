package dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import models.deactivateStudent
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.nio.file.Files
import java.nio.file.Paths

@Composable
fun datenHolenWindow(onDismiss: () -> Unit) {

    var requirePassword by remember { mutableStateOf(true) }

    if (requirePassword) {
        passwordDialog(
            result = { pwCorrect -> requirePassword = !pwCorrect }, // if password correct, set requirePasswort to false
            onDissmiss = onDismiss
        )
    } else {
        Dialog(
            state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 750.dp, height = 600.dp),
            title = "Daten holen",
            onCloseRequest = onDismiss
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val reader = Files.newBufferedReader(Paths.get("src/jvmMain/resources/transferHauseDojo.csv"))

                val csvParser = CSVParser(
                    reader, CSVFormat.DEFAULT.withDelimiter(';')
                        .withFirstRecordAsHeader()
                )

                // csv structure: Name;Gruppe;Grad;Geb.Dat;e;f;g
                csvParser
                for (csvRecord in csvParser) {
                    // Accessing Values by Column Index
                    val name = csvRecord.get(0)
                    val group = csvRecord.get(1)
                    val level = csvRecord.get(2)
                    val birthday = csvRecord.get(3)
                    val oldName = csvRecord.get(4)
                    val newName = csvRecord.get(5)
                    val exMember = csvRecord.get(6)
                    // print the value to console
                    println("Record No - " + csvRecord.recordNumber)
                    println("---------------")
                    println("Name : $name")
                    println("Product : $group")
                    println("Description : $level")
                    println("---------------")

                    if (exMember != null) {
                        val splitName = exMember.split(" ")
                        val prename = splitName.joinToString(" ", limit = splitName.size - 1, truncated = "").trim()
                        val surname = splitName[splitName.size - 1].trim()
                        Text("$prename|$surname")
                        deactivateStudent(prename, surname)
                    }

                }
            }
        }

    }
}