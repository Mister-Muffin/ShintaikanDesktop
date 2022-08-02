package dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.deactivateStudent
import models.renameStudent
import models.updateStudent
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
        coroutineScope.launch(Dispatchers.IO) {
            exMembers(textFieldValue)
            renameMembers(textFieldValue)
            updateMembers(textFieldValue)
        }.invokeOnCompletion {
            textFieldValue.value = "Complete!"
        }

        Dialog(
            state = rememberDialogState(position = WindowPosition(Alignment.Center), width = 600.dp, height = 250.dp),
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
                Text("Bitte warten...")
                Spacer(modifier = Modifier.height(8.dp))
                Text(textFieldValue.value)

            }
        }

    }
}

private const val csvPath = "/home/julian/Entwicklung/transferHauseDojo.CSV"

private suspend fun exMembers(text: MutableState<String>) {
    val reader = withContext(Dispatchers.IO) {
        Files.newBufferedReader(Paths.get(csvPath))
    }
    val csvParser = CSVParser(
        reader, CSVFormat.DEFAULT
            .withDelimiter(';')
            .withFirstRecordAsHeader()
            .withIgnoreHeaderCase()
            .withTrim()
    )
    for (csvRecord in csvParser) {
        try {
            // Accessing Values by Column Index
            val name = csvRecord.get(0)

            if (name == "ZZähler") return // stop if end of csv file is reached

            val exMember = csvRecord.get(6)
            // print the value to console
            println("Record No - " + csvRecord.recordNumber)
            println("---------------")
            println("Name : $name")
            println("Ex Member : $exMember")
            println("---------------")

            if (!exMember.isNullOrEmpty()) {
                val exMemberName = splitName(exMember)
                text.value = "${exMemberName.first}|${exMemberName.second}"
                deactivateStudent(exMemberName)
            }

        } catch (_: ArrayIndexOutOfBoundsException) {
            println("AIOOB")
            return
        }
    }
}


private suspend fun renameMembers(text: MutableState<String>) {
    val reader = withContext(Dispatchers.IO) {
        Files.newBufferedReader(Paths.get(csvPath))
    }
    val csvParser = CSVParser(
        reader, CSVFormat.DEFAULT
            .withDelimiter(';')
            .withFirstRecordAsHeader()
            .withIgnoreHeaderCase()
            .withTrim()
    )
    for (csvRecord in csvParser) {
        try {
            // Accessing Values by Column Index
            val name = csvRecord.get(0)

            if (name == "ZZähler") return // stop if end of csv file is reached

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
            }


        } catch (_: ArrayIndexOutOfBoundsException) {
            println("AIOOB")
            return
        }
    }
}

private suspend fun updateMembers(text: MutableState<String>) {
    val reader = withContext(Dispatchers.IO) {
        Files.newBufferedReader(Paths.get(csvPath))
    }
    val csvParser = CSVParser(
        reader, CSVFormat.DEFAULT
            .withDelimiter(';')
            .withFirstRecordAsHeader()
            .withIgnoreHeaderCase()
            .withTrim()
    )
    for (csvRecord in csvParser) {
        try {
            // Accessing Values by Column Index
            val name = csvRecord.get(0)

            if (name == "ZZähler") return // stop if end of csv file is reached

            val group = csvRecord.get(1)
            val level = csvRecord.get(2)
            val birthday = csvRecord.get(3)
            // Print values to console
            println("Record No - " + csvRecord.recordNumber)
            println("---------------")
            println("Name : $name")
            println("Group : $group")
            println("Level : $level")
            println("Birthday : $birthday")
            println("---------------")

            if (!name.isNullOrEmpty()) {
                val namePair = splitName(name)

                text.value = name
                updateStudent(namePair, group, level, birthday)
            }

        } catch (_: ArrayIndexOutOfBoundsException) {
            println("AIOOB")
            return
        }
    }
}

/**
 * Returns a Pair of strings, where the first part is the prename and the second part is the surname
 *
 * Splitting works by splitting the full name with the given delimiter,
 * taking the first item from the split array as the surname and the rest as prenames
 *
 * Example: Mustermann Max Munter -> Pair(Max Munter, Mustermann)
 *
 * @param name Full name to split
 * @param delimiters Delimiter at whitch the name will be split (defaults to " ")
 * @return Pair<prename: String, surname: String>
 */
private fun splitName(name: String, delimiters: String = " "): Pair<String, String> {
    var splitName = name.split(delimiters)
    val surname = splitName[0].trim()
    splitName = splitName.drop(1) // drop first item in array (index 0)
    val prename = splitName.joinToString(" ", truncated = "").trim()
    return Pair(prename, surname)
}