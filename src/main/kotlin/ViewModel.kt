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


            allMembers.addAll(members)
            allMessages.addAll(messages)
            birthdays.addAll(loadBirthdays(members))
            this@ViewModel.trainers.addAll(trainers)
            this@ViewModel.teilnahme.addAll(teilnahme)
        }
    }

    fun reloadMessages() {
        coroutineScope.launch {
            val messages = loadMessages()
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