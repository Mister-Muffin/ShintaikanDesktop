import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import models.*
import java.time.LocalDate

class ViewModel(val coroutineScope: CoroutineScope) {

    val allMembers = mutableStateListOf<Member>()
    val allMessages = mutableStateListOf<Message>()
    val birthdays = mutableStateListOf<Member>()
    val trainers = mutableStateListOf<Trainer>()
    val teilnahme = mutableStateListOf<Teilnahme>()

    fun loadAll() {
        coroutineScope.launch {
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
        }
    }

    fun reloadMembers() {
        coroutineScope.launch {
            val members = loadMembers()
            allMembers.clear()
            allMembers.addAll(members)
        }
    }

    fun reloadMessages() {
        coroutineScope.launch {
            val messages = loadMessages()
            allMessages.clear()
            allMessages.addAll(messages)
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

}
