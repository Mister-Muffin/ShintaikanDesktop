package viewmodel

import configFilePath
import models.*
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalTime

suspend fun dumpCurrentDatabase() {
    val writer = Files.newBufferedWriter(Paths.get("${configFilePath}backups/backup-${LocalTime.now()}.csv"))

    val csvPrinter = CSVPrinter(
        writer, CSVFormat.DEFAULT
        //.withHeader(StudentTable.columns)
    )

    val members = loadMembers(false)
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

suspend fun exMembers(setText: (String) -> Unit, csvParser: CSVParser) {
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

suspend fun renameMembers(setText: (String) -> Unit, csvParser: CSVParser) {
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

suspend fun updateMembers(setText: (String) -> Unit, csvParser: CSVParser) {
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
