package viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import model.Member
import model.Message
import model.Participation
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import kotlin.math.absoluteValue
import kotlin.time.Duration

class ViewModel(private val coroutineScope: CoroutineScope) {

    var dataLoading by mutableStateOf(true)
    var loadTime = Duration.ZERO

    private val mutableMembers = mutableStateListOf<Member>()
    val members: List<Member> by lazy {
        coroutineScope.launch {
            mutableMembers.addAll(database.loadMembers())
        }
        mutableMembers
    }

    private val mutableMessages = mutableStateListOf<Message>()
    val messages: List<Message> by lazy {
        coroutineScope.launch {
            mutableMessages.addAll(database.loadMessages())
        }
        mutableMessages
    }

    private val mutableParticipations = mutableStateListOf<Participation>()
    val participations: List<Participation> by lazy {
        coroutineScope.launch {
            mutableParticipations.addAll(database.loadParticipations())
        }
        mutableParticipations
    }

    //<editor-fold desc="Message operations">
    fun addMessage(newMessage: String): Message {
        val temporaryMessage = Message(-1, newMessage, "", LocalDate.now())
        val id = runBlocking { database.insertMessage(temporaryMessage) }
        val message = temporaryMessage.copy(id = id)
        mutableMessages.add(message)
        return message
    }

    fun deleteMessage(id: Int) {
        coroutineScope.launch { database.deleteMessage(id) }
        mutableMessages.removeIf { it.id == id }
    }

    fun updateMessage(updatedMessage: Message) {
        coroutineScope.launch { database.updateMessage(updatedMessage) }
        mutableMessages.apply {
            removeIf { it.id == updatedMessage.id }
            add(updatedMessage)
        }
    }
    //</editor-fold>

    //<editor-fold desc="Member operations">
    fun clearUnitsSinceLastExam(member: Member) {
        coroutineScope.launch { database.clearUnitsSinceLastExam(member) }
        mutableMembers.remove(member)
        mutableMembers.add(member.copy(unitsSinceLastExam = 0))
    }

    fun deactivateMember(member: Member) {
        coroutineScope.launch { database.deactivateMember(member) }
        mutableMembers.remove(member)
        mutableMembers.add(member.copy(isActive = false))
    }

    fun updateMemberName(member: Member, firstName: String, lastName: String) {
        coroutineScope.launch { database.updateMemberName(member, firstName, lastName) }
        mutableMembers.remove(member)
        mutableMembers.add(member.copy(prename = firstName, surname = lastName))
    }

    fun upsertMemberData(member: Member, group: String, level: String, birthday: LocalDate) {
        if (member.id >= 0) {
            coroutineScope.launch { database.updateMemberData(member, group, level, birthday) }
            mutableMembers.remove(member)
            mutableMembers.add(member.copy(group = group, level = level, birthday = birthday))
        } else {
            val id = runBlocking { database.insertMember(member) }
            mutableMembers.add(member.copy(id = id))
        }
    }

    fun setTrainerStatus(member: Member, isTrainer: Boolean) {
        coroutineScope.launch { database.setTrainerStatus(member, isTrainer) }
        mutableMembers.remove(member)
        mutableMembers.add(member.copy(isTrainer = isTrainer))
    }

    fun updateStickers(member: Member, stickerNumber: Int, stickerData: String) {
        val updateStickerData = "${member.stickerReceivedBy},$stickerData".trim(',')
        val today = LocalDate.now()
        coroutineScope.launch { database.updateStickers(member, stickerNumber, updateStickerData, today) }
        mutableMembers.remove(member)
        mutableMembers.add(member.copy(stickerReceivedBy = updateStickerData, receivedStickerNumber = stickerNumber))
    }

    fun incrementTrainerUnits(member: Member) {
        coroutineScope.launch { database.incrementTrainerUnits(member) }
        mutableMembers.remove(member)
        mutableMembers.add(member.copy(trainerUnits = member.trainerUnits + 1))
    }

    fun getBirthdayMembers(): List<Member> {
        val now = LocalDate.now()
        return members.filter {
            it.birthday.until(now).days.absoluteValue <= 3
        }
    }

    fun getTrainers(): List<Member> {
        return members.filter(Member::isTrainer)
    }

    fun getLastExamDate(id: Int): LocalDate {
        return participations.filter { id.toString() in it.userIdsExam }.maxBy { it.date }.date
    }
    //</editor-fold>

    //<editor-fold desc="Participation operations">
    fun addParticipation(participants: String, isExam: Boolean): Participation {
        // TODO: Heilige Scheiße, ist das schlimm...
        val today = LocalDate.now()
        val participationToday = participations.firstOrNull { it.date == today }
        val participantsString = (participationToday?.userIds?.let { "$it," } ?: "") + if (!isExam) participants else ""
        val examParticipantString =
            (participationToday?.userIdsExam?.let { "$it," } ?: "") + if (isExam) participants else ""
        val temporaryParticipation =
            Participation(-1, participantsString.trim(','), examParticipantString.trim(','), today)
        val id = runBlocking { database.addParticipation(temporaryParticipation) }
        val participation = temporaryParticipation.copy(id = id)

        if (participationToday != null) {
            mutableParticipations.remove(participationToday)
        }

        mutableParticipations.add(participation)
        return participation
    }
    //</editor-fold>

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

            dumpCurrentDatabase(members, messages, participations)
            exMembers(
                setText,
                { f, l -> members.find { it.prename == f && it.surname == l }?.let { deactivateMember(it) } },
                csvParser1
            )
            renameMembers(
                setText,
                { (oldFirst, oldLast), (newFirst, newLast) ->
                    members.find { it.prename == oldFirst && it.surname == oldLast }
                        ?.let { updateMemberName(it, newFirst, newLast) }
                }, csvParser2
            )
            updateMembers(
                setText,
                { (first, last), group, level, birthday ->
                    members.find { it.prename == first && it.surname == last }.let {
                        it ?: Member(
                            -1,
                            first,
                            last,
                            group,
                            level,
                            0,
                            LocalDate.parse(birthday),
                            null,
                            false,
                            null,
                            0,
                            null,
                            null,
                            true, // TODO: Confirm
                            0,
                            0
                        )
                    }.let {
                        upsertMemberData(
                            it,
                            group,
                            level,
                            LocalDate.parse(birthday)
                        )
                    } // Es besteht Hoffnung, dass das mit dem Date einfach so funktioniert
                }, csvParser3
            )

            dataLoading = false
            onComplete()
        }
    }

    private val database = object {
        //<editor-fold desc="Message operations">
        suspend fun loadMessages(): List<Message> {
            return suspendedTransactionAsync(Dispatchers.IO) {
                Message.selectAll().map(Message::fromRow)
            }.await()
        }

        suspend fun insertMessage(message: Message): Int {
            return suspendedTransactionAsync {
                Message.insertAndGetId {
                    message.updateInto(it)
                }.value
            }.await()
        }

        suspend fun deleteMessage(id: Int) {
            suspendedTransactionAsync(Dispatchers.IO) {
                Message.deleteWhere {
                    Message.id eq id
                }
            }.await()
        }

        suspend fun updateMessage(message: Message) {
            suspendedTransactionAsync(Dispatchers.IO) {
                Message.update(where = { Message.id eq message.id }) {
                    message.updateInto(it)
                }
            }.await()
        }
        //</editor-fold>

        //<editor-fold desc="Participation operations">
        suspend fun loadParticipations(): List<Participation> {
            return suspendedTransactionAsync {
                Participation.selectAll().map(Participation::fromRow)
            }.await()
        }

        suspend fun addParticipation(temporaryParticipation: Participation): Int {
            return suspendedTransactionAsync {
                if (temporaryParticipation.id < 0) {
                    Participation.insertAndGetId {
                        temporaryParticipation.upsertInto(it)
                    }.value
                } else {
                    val today = LocalDate.now()
                    Participation.update({ Participation.date eq today }) {
                        temporaryParticipation.upsertInto(it)
                    }
                    temporaryParticipation.id
                }
            }.await()
        }
        //</editor-fold>


        //<editor-fold desc="Member operations">
        suspend fun loadMembers(): List<Member> {
            return suspendedTransactionAsync {
                Member.selectAll().map {
                    Member.fromRow(it, getLastExamDate = { getLastExamDate(it) })
                }
            }.await()
        }

        suspend fun clearUnitsSinceLastExam(member: Member) {
            suspendedTransactionAsync {
                Member.update(where = { Member.id eq member.id }) {
                    it[unitsSinceLastExam] = 0
                }
            }.await()
        }

        suspend fun deactivateMember(member: Member) {
            suspendedTransactionAsync {
                Member.update(where = { Member.id eq member.id }) {
                    it[isActive] = false
                }
            }.await()
        }

        suspend fun updateMemberName(member: Member, firstName: String, lastName: String) {
            suspendedTransactionAsync {
                Member.update(where = { Member.id eq member.id }) {
                    it[prename] = firstName
                    it[surname] = lastName
                }
            }.await()
        }

        suspend fun insertMember(member: Member): Int {
            return suspendedTransactionAsync {
                Member.insertAndGetId {
                    member.upsertInto(it)
                }.value
            }.await()
        }

        suspend fun updateMemberData(member: Member, group: String, level: String, birthday: LocalDate) {
            suspendedTransactionAsync {
                Member.update(where = { Member.id eq member.id }) {
                    it[Member.group] = group
                    it[Member.level] = level
                    it[Member.birthday] = birthday
                }
            }.await()
        }

        suspend fun setTrainerStatus(member: Member, isTrainer: Boolean) {
            suspendedTransactionAsync {
                Member.update(where = { Member.id eq member.id }) {
                    it[Member.isTrainer] = isTrainer
                }
            }.await()
        }

        suspend fun updateStickers(member: Member, stickerNumber: Int, stickerData: String, stickerDate: LocalDate) {
            suspendedTransactionAsync {
                Member.update(where = { Member.id eq member.id }) {
                    it[stickerReceived] = stickerNumber
                    it[stickerReceivedBy] = stickerData
                    it[stickerDateReceived] = stickerDate
                }
            }.await()
        }

        suspend fun incrementTrainerUnits(member: Member) {
            suspendedTransactionAsync {
                Member.update(where = { Member.id eq member.id }) {
                    with(SqlExpressionBuilder) {
                        it.update(trainerUnits, trainerUnits + 1)
                    }
                }
            }.await()
        }
        //</editor-fold>
    }
}
