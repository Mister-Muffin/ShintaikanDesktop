package viewmodel

import configFilePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.Member
import model.Message
import model.Participation
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalTime

suspend fun dumpCurrentDatabase(members: List<Member>, messages: List<Message>, participations: List<Participation>) {
    val writer = withContext(Dispatchers.IO) {
        Files.newBufferedWriter(Paths.get("${configFilePath}backups/backup-${LocalTime.now()}.csv"))
    }

    val csvPrinter = CSVPrinter(
        writer, CSVFormat.DEFAULT
        //.withHeader(StudentTable.columns)
    )

    members.forEach { member ->
        csvPrinter.printRecord(
            member.id,
            member.prename,
            member.surname,
            member.group,
            member.level,
            member.total,
            member.birthday,
            member.isTrainer,
            member.lastExamDate,
            member.stickerAnimal,
            member.stickerDateReceived,
            member.stickerReceivedBy,
            member.receivedStickerNumber,
            member.isActive,
            member.trainerUnits
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
    participations.forEach {
        csvPrinter.printRecord(
            it.id,
            it.date,
            it.memberId,
            it.note,
            it.exam
        )
    }
    //csvPrinter.printRecords(teilnahme)
    csvPrinter.flush()
    csvPrinter.close()
}

suspend fun exMembers(
    setText: (String) -> Unit,
    deactivateMember: (String, String) -> Unit,
    csvParser: CSVParser
) {
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
                deactivateMember(exMemberName.first, exMemberName.second)
            }

        } catch (_: ArrayIndexOutOfBoundsException) {
            println("AIOOB")
            return
        }
    }
}

suspend fun renameMembers(
    setText: (String) -> Unit,
    renameMember: (Pair<String, String>, Pair<String, String>) -> Unit,
    csvParser: CSVParser
) {
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

suspend fun updateMembers(
    setText: (String) -> Unit,
    updateMember: (Pair<String, String>, String, String, String) -> Unit,
    csvParser: CSVParser
) {
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
