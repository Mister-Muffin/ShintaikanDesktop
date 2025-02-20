package viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import model.Member
import model.Message
import model.OldParticipation
import model.Participation
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate

class ViewModel(private val coroutineScope: CoroutineScope) {

    var dataLoading by mutableStateOf(false)

    private val mutableMembers = mutableStateListOf<Member>()
    val members: List<Member> by lazy {
        runBlocking {
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
        // runBlocking because with coroutine, lastExamDate cannot be calculated
        runBlocking {
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

    private fun increaseUnitsSinceLastExam(member: Member, count: Int = 1) {
        coroutineScope.launch { database.increaseUnitsSinceLastExam(member, count) }
        mutableMembers.remove(member)
        mutableMembers.add(member.copy(unitsSinceLastExam = member.unitsSinceLastExam + count))
    }

    fun getBirthdayMembers(): List<Member> {
        val now = LocalDate.now()
        return members.filter {
            it.birthday.plusYears((now.year - it.birthday.year).toLong()) in now.minusDays(3)..now.plusDays(3)
        }
    }

    fun getTrainers(): List<Member> {
        return members.filter(Member::isTrainer)
    }

    fun getLastExamDate(id: Int, memberLastExamDate: LocalDate?): LocalDate? {
        // Filtert alle Participation-Einträge, die der memberId entsprechen und eine Prüfung haben
        val participationExams = participations.filter { it.memberId == id && it.exam }

        // Findet das größte Datum in participationExams, falls vorhanden
        val latestParticipationExam = participationExams.maxByOrNull { it.date }?.date

        // Gibt das größte der beiden Daten zurück oder null, falls beide null sind
        return when {
            memberLastExamDate == null -> latestParticipationExam  // Wenn memberLastExamDate null ist, returne participationExam (kann auch null sein)
            latestParticipationExam == null -> memberLastExamDate  // Wenn participationExam null ist, returne memberLastExamDate
            else -> maxOf(memberLastExamDate, latestParticipationExam)  // Ansonsten das größere Datum der beiden
        }
    }
    //</editor-fold>

    fun migrateTable() {
        runBlocking {
            database.migrateTable()
        }
    }

    //<editor-fold desc="Participation operations">
    fun addParticipation(participant: Member, isExam: Boolean, note: String, trainerId: Int) {
        val today = LocalDate.now()

        val tmpParticipation = Participation(-1, participant.id, today, note, isExam, trainerId)

        val id = runBlocking { database.addParticipation(tmpParticipation) }

        val participation = tmpParticipation.copy(id = id)
        mutableParticipations.add(participation)

        // if (!isExam) increaseUnitsSinceLastExam(participant)
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
                            true,
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

        suspend fun migrateTable() {
            return suspendedTransactionAsync(Dispatchers.IO) {
                val parts = loadOldParticipations()
                parts.forEach outer@{ part ->
                    val ids = part.userIds.split(',')
                    val examIds = part.userIdsExam.split(',')

                    ids.forEach { id ->
                        if (id.isBlank()) return@forEach
                        if (id == "null") return@forEach

                        val tmpPre = Participation(-1, id.toInt(), part.date, "", false, null)
                        Participation.insertAndGetId {
                            tmpPre.insertInto(it)
                        }.value
                    }

                    examIds.forEach { id ->
                        if (id.isBlank()) return@forEach
                        if (id == "null") return@forEach

                        val tmpPre = Participation(-1, id.toInt(), part.date, "", true, null)
                        Participation.insertAndGetId {
                            tmpPre.insertInto(it)
                        }.value
                    }
                }
            }.await()
        }

        //<editor-fold desc="Participation operations">
        suspend fun loadParticipations(): List<Participation> {
            return suspendedTransactionAsync(Dispatchers.IO) {
                Participation.selectAll().map(Participation::fromRow)
            }.await()
        }

        suspend fun loadOldParticipations(): List<OldParticipation> {
            return suspendedTransactionAsync(Dispatchers.IO) {
                OldParticipation.selectAll().map(OldParticipation::fromRow)
            }.await()
        }

        suspend fun addParticipation(temporaryParticipation: Participation): Int {
            return suspendedTransactionAsync(Dispatchers.IO) {
                Participation.insertAndGetId {
                    temporaryParticipation.insertInto(it)
                }.value
            }.await()
        }
        //</editor-fold>


        //<editor-fold desc="Member operations">
        suspend fun loadMembers(): List<Member> {
            return suspendedTransactionAsync(Dispatchers.IO) {
                Member.selectAll().map {
                    Member.fromRow(it, ::getLastExamDate)
                }
            }.await()
        }

        suspend fun clearUnitsSinceLastExam(member: Member) {
            suspendedTransactionAsync(Dispatchers.IO) {
                Member.update(where = { Member.id eq member.id }) {
                    it[unitsSinceLastExam] = 0
                }
            }.await()
        }

        suspend fun deactivateMember(member: Member) {
            suspendedTransactionAsync(Dispatchers.IO) {
                Member.update(where = { Member.id eq member.id }) {
                    it[isActive] = false
                }
            }.await()
        }

        suspend fun updateMemberName(member: Member, firstName: String, lastName: String) {
            suspendedTransactionAsync(Dispatchers.IO) {
                Member.update(where = { Member.id eq member.id }) {
                    it[prename] = firstName
                    it[surname] = lastName
                }
            }.await()
        }

        suspend fun insertMember(member: Member): Int {
            return suspendedTransactionAsync(Dispatchers.IO) {
                Member.insertAndGetId {
                    member.upsertInto(it)
                }.value
            }.await()
        }

        suspend fun updateMemberData(member: Member, group: String, level: String, birthday: LocalDate) {
            suspendedTransactionAsync(Dispatchers.IO) {
                Member.update(where = { Member.id eq member.id }) {
                    it[Member.group] = group
                    it[Member.level] = level
                    it[Member.birthday] = birthday
                }
            }.await()
        }

        suspend fun setTrainerStatus(member: Member, isTrainer: Boolean) {
            suspendedTransactionAsync(Dispatchers.IO) {
                Member.update(where = { Member.id eq member.id }) {
                    it[Member.isTrainer] = isTrainer
                }
            }.await()
        }

        suspend fun updateStickers(member: Member, stickerNumber: Int, stickerData: String, stickerDate: LocalDate) {
            suspendedTransactionAsync(Dispatchers.IO) {
                Member.update(where = { Member.id eq member.id }) {
                    it[stickerReceived] = stickerNumber
                    it[stickerReceivedBy] = stickerData
                    it[stickerDateReceived] = stickerDate
                }
            }.await()
        }

        suspend fun incrementTrainerUnits(member: Member) {
            suspendedTransactionAsync(Dispatchers.IO) {
                Member.update(where = { Member.id eq member.id }) {
                    with(SqlExpressionBuilder) {
                        it.update(trainerUnits, trainerUnits + 1)
                    }
                }
            }.await()
        }

        suspend fun increaseUnitsSinceLastExam(member: Member, count: Int) {
            suspendedTransactionAsync(Dispatchers.IO) {
                Member.update(where = { Member.id eq member.id }) {
                    with(SqlExpressionBuilder) {
                        it.update(unitsSinceLastExam, unitsSinceLastExam + count)
                    }
                }
            }.await()
        }
        //</editor-fold>
    }
}
