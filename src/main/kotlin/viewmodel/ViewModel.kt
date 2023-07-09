package viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.*
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate

class ViewModel(val coroutineScope: CoroutineScope) {

    val allMembers = mutableStateListOf<Member>()
    val allMessages = mutableStateListOf<Message>()
    val birthdays = mutableStateListOf<Member>()
    val trainers = mutableStateListOf<Trainer>()
    val teilnahme = mutableStateListOf<Teilnahme>()

    var dataLoading by mutableStateOf(true)

    fun loadAll() {
        coroutineScope.launch {
            dataLoading = true

            val members = loadMembers()
            val messages = loadMessages()
            val trainers = loadTrainers()
            val teilnahme = loadTeilnahme()

            allMembers.clear()
            allMessages.clear()
            birthdays.clear()
            this@ViewModel.trainers.clear()
            this@ViewModel.teilnahme.clear()

            allMembers.addAll(members)
            allMessages.addAll(messages)
            birthdays.addAll(loadBirthdays(members))
            this@ViewModel.trainers.addAll(trainers)
            this@ViewModel.teilnahme.addAll(teilnahme)

            dataLoading = false
        }
    }

    fun reloadMembers() {
        coroutineScope.launch {
            dataLoading = true

            val members = loadMembers()
            allMembers.clear()
            allMembers.addAll(members)

            dataLoading = false
        }
    }

    fun reloadMessages() {
        coroutineScope.launch {
            dataLoading = true

            val messages = loadMessages()
            allMessages.clear()
            allMessages.addAll(messages)

            dataLoading = false
        }
    }

    fun submitNewMessage(newMessage: String) {
        val newMessageObj = Message(-1, newMessage, "", LocalDate.now())
        val id = addMessage(newMessageObj)
        allMessages.add(
            Message(
                id = id,
                message = newMessage,
                short = "",
                newMessageObj.dateCreated
            )
        )
    }

    fun insertTeilnahme(insertString: String, isExam: Boolean) {
        coroutineScope.launch {
            models.insertTeilnahme(insertString, isExam)
        }
    }

    private fun loadBirthdays(members: List<Member>): MutableList<Member> {
        val birthdays = mutableListOf<Member>()

        for (student in members) {
            val birthday = student.birthday.plusYears((LocalDate.now().year - student.birthday.year).toLong())
            if (birthday >= (LocalDate.now().minusDays(3)) &&
                birthday <= LocalDate.now().plusDays(3)
            ) {
                birthdays.add(student)
            }
        }
        return birthdays
    }

    /**
     * csv structure: Name;Gruppe;Grad;Geb.Dat;e;f;g
     *
     *                0   ;  1   ; 2  ; 3     ;4;5;6
     *
     * // exMembers(csvParser)
     */
    fun fetchData(csvPath: String, setText: (String) -> Unit, onComplete: () -> Unit) {
        dataLoading = true
        coroutineScope.launch {
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
            exMembers(setText, csvParser1)
            renameMembers(setText, csvParser2)
            updateMembers(setText, csvParser3)

            dataLoading = false
            onComplete()
        }
    }
}
