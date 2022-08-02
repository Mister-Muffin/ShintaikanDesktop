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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import models.StudentTable.prename
import models.StudentTable.surname
import models.deactivateStudent
import models.renameStudent
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.nio.file.Files
import java.nio.file.Paths

@Composable
fun datenHolenWindow(onDismiss: () -> Unit) {

    var requirePassword by remember { mutableStateOf(true) }

    val textFieldValue = remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    if (requirePassword) {
        passwordDialog(
            result = { pwCorrect -> requirePassword = !pwCorrect }, // if password correct, set requirePasswort to false
            onDissmiss = onDismiss
        )
    } else {
        val reader = Files.newBufferedReader(Paths.get("src/jvmMain/resources/transferHauseDojo.CSV"))

        val csvParser = CSVParser(
            reader, CSVFormat.DEFAULT
                .withDelimiter(';')
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim()
        )

        coroutineScope.launch(Dispatchers.IO) {
            renameMembers(csvParser, textFieldValue)
        }.invokeOnCompletion {
            textFieldValue.value = "Complete"
        }

        Dialog(
            state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 750.dp, height = 600.dp),
            title = "Daten holen",
            onCloseRequest = onDismiss
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // csv structure: Name;Gruppe;Grad;Geb.Dat;e;f;g
                //                0   ;  1   ; 2  ; 3     ;4;5;6
                //exMembers(csvParser)
                Text(textFieldValue.value)

            }
        }

    }
}

private suspend fun exMembers(csvParser: CSVParser, text: MutableState<String>) {
    for (csvRecord in csvParser) {
        try {
            // Accessing Values by Column Index
            val name = csvRecord.get(0)
            val exMember = csvRecord.get(6)
            // print the value to console
            println("Record No - " + csvRecord.recordNumber)
            println("---------------")
            println("Name : $name")
            println("Ex Member : $exMember")
            println("---------------")

            if (!exMember.isNullOrEmpty()) {
                val splitName = exMember.split(" ")
                val prename = splitName.joinToString(" ", limit = splitName.size - 1, truncated = "").trim()
                val surname = splitName[splitName.size - 1].trim()
                deactivateStudent(prename, surname)
            }
            text.value = "$prename|$surname"

        } catch (_: ArrayIndexOutOfBoundsException) {
        }
    }
}


private suspend fun renameMembers(csvParser: CSVParser, text: MutableState<String>) {
    for (csvRecord in csvParser) {
        try {
            // Accessing Values by Column Index
            val name = csvRecord.get(0)
            val oldName = csvRecord.get(4)
            val newName = csvRecord.get(5)
            // Print values to console
            println("Record No - " + csvRecord.recordNumber)
            println("---------------")
            println("Name : $name")
            println("Old Name : $oldName")
            println("New Name : $newName")
            println("---------------")

            if (!oldName.isNullOrEmpty() && !newName.isNullOrEmpty()) {
                val oldName1 = splitName(oldName)
                val newName1 = splitName(newName)

                text.value = "${oldName1.first}|${oldName1.second}"
                renameStudent(oldName1, newName1)
                delay(1000)
            }


        } catch (_: ArrayIndexOutOfBoundsException) {
        }
    }
}

private fun splitName(name: String, delimiters: String = " "): Pair<String, String> {
    var splitName = name.split(delimiters)
    val surname = splitName[0].trim()
    splitName = splitName.drop(1) // drop first item in array (index 0)
    val prename = splitName.joinToString(" ", truncated = "").trim()
    return Pair(prename, surname)
}