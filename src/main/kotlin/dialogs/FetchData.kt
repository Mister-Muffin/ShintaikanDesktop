package dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import configFilePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import models.*
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import java.nio.file.Files
import java.nio.file.Files.newBufferedWriter
import java.nio.file.Paths
import java.time.LocalTime

private const val textWhenDone = "Complete!\nExit Program."

@Composable
fun FetchDataWindow(drivePath: String, onDismiss: () -> Unit) {
    var textFieldValue by remember { mutableStateOf("") }

    val csvPath = "${drivePath}transferHauseDojo.csv"

    LaunchedEffect(Unit) {
        //coroutineScope.launch(Dispatchers.IO) {
        val reader1 = withContext(Dispatchers.IO) {
            Files.newBufferedReader(Paths.get(csvPath))
        }
        val reader2 = withContext(Dispatchers.IO) {
            Files.newBufferedReader(Paths.get(csvPath))
        }
        val reader3 = withContext(Dispatchers.IO) {
            Files.newBufferedReader(Paths.get(csvPath))
        }

        val csvParser1 = CSVParser(
            reader1, CSVFormat.DEFAULT
                .withDelimiter(';')
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim()
        )
        val csvParser2 = CSVParser(
            reader2, CSVFormat.DEFAULT
                .withDelimiter(';')
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim()
        )
        val csvParser3 = CSVParser(
            reader3, CSVFormat.DEFAULT
                .withDelimiter(';')
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim()
        )

        dumpCurrentDatabase()
        exMembers({ textFieldValue = it }, csvParser1)
        renameMembers({ textFieldValue = it }, csvParser2)
        updateMembers({ textFieldValue = it }, csvParser3)

        textFieldValue = textWhenDone
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // csv structure: Name;Gruppe;Grad;Geb.Dat;e;f;g
        //                0   ;  1   ; 2  ; 3     ;4;5;6
        //exMembers(csvParser)
        Text("Bitte warten...")
        Spacer(modifier = Modifier.height(8.dp))
        Text(textFieldValue)
        Spacer(modifier = Modifier.fillMaxHeight(.5f))
        Button(enabled = textFieldValue == textWhenDone, onClick = onDismiss) {
            Text("Zurück")
        }

    }
}

private suspend fun dumpCurrentDatabase() {
    val writer = newBufferedWriter(Paths.get("${configFilePath}backups/backup-${LocalTime.now()}.csv"))

    val csvPrinter = CSVPrinter(
        writer, CSVFormat.DEFAULT
        //.withHeader(StudentTable.columns)
    )

    val members = loadFullMemberTable()
    val messages = loadMessages()
    val teilnahme = loadTeilnahme()

    members.forEach { member ->
        csvPrinter.printRecord(
            member.id,
            member.prename,
            member.surname,
            member.group,
            member.level,
            member.total,
            member.birthday,
            member.is_trainer,
            member.date_last_exam,
            member.sticker_animal,
            member.sticker_date_recieved,
            member.sticker_recieved_by,
            member.sticker_recieved,
            member.is_active,
            member.trainer_units
        )

    }
    //csvPrinter.printRecords(members)
    csvPrinter.println()
    messages.forEach {
        csvPrinter.printRecord(
            it.id,
            it.message,
            it.short,
            it.dateCreated
        )
    }
    //csvPrinter.printRecords(messages)
    csvPrinter.println()
    teilnahme.forEach {
        csvPrinter.printRecord(
            it.id,
            it.date,
            it.userIds,
            it.userIdsExam
        )
    }
    //csvPrinter.printRecords(teilnahme)
    csvPrinter.flush()
    csvPrinter.close()
}

private suspend fun exMembers(setText: (String) -> Unit, csvParser: CSVParser) {
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
                setText("${exMemberName.first}|${exMemberName.second}")
                deactivateMember(exMemberName)
            }

        } catch (_: ArrayIndexOutOfBoundsException) {
            println("AIOOB")
            return
        }
    }
}


private suspend fun renameMembers(setText: (String) -> Unit, csvParser: CSVParser) {
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

                setText("${oldName1.first}|${oldName1.second}")
                renameMember(oldName1, newName1)
            }


        } catch (_: ArrayIndexOutOfBoundsException) {
            println("AIOOB")
            return
        }
    }
}

private suspend fun updateMembers(setText: (String) -> Unit, csvParser: CSVParser) {
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

                setText(name)
                updateMember(namePair, group, level, birthday)
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
