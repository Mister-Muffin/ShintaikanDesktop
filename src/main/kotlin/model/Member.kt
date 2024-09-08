package model

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.time.LocalDate
import java.time.Period

object MemberTable

data class Member(
    val id: Int,
    val surname: String,
    val prename: String,
    val group: String,
    val level: String,
    val total: Int, // TODO: Shit name
    val birthday: LocalDate,
    val lastExamDate: LocalDate?,
    val isTrainer: Boolean,
    val stickerAnimal: String?,
    val receivedStickerNumber: Int,
    val stickerDateReceived: LocalDate?,
    val stickerReceivedBy: String?,
    val isActive: Boolean,
    val trainerUnits: Int,
    val unitsSinceLastExam: Int,
    val radioClicked: Boolean = false, // for sticker dialog (all radio buttons must be clicked before button activated). Ooof, s.u.
    val stickerReceived: Boolean = false, // for sticker dialog, if radio button is checked or not. Ooof, there surely exists a better modelling for this
    val stickerShowAgain: Boolean = true, // for sticker dialog, if student is still missing stickers and the dialog should open again with this student
) {
    val age: Int = Period.between(birthday, LocalDate.now()).years

    /**
     * Example: z Kyu weiss -> Kyu weiss
     * @return The member's level formatted to be displayed in the UI
     */
    fun formattedLevel(): String {
        var level = this.level
        if (level.contains("Dan")) {
            level = level.drop(2) // drop the first two letters
        } else if (level == "z Kyu weiss") {
            level = level.drop(2)
        }
        return level
    }

    companion object : IntIdTable("main") {
        val surname = text("surname")
        val prename = text("prename")
        val group = text("group")
        val level = text("level")
        val total = integer("total")
        val birthday = date("birthday")
        val lastExamDate = date("date_last_exam")
        val isTrainer = bool("is_trainer")
        val stickerAnimal = text("sticker_animal")
        val stickerReceived = integer("sticker_recieved") // TODO: So schreibt man received nicht
        val stickerDateReceived = date("sticker_date_recieved")
        val stickerReceivedBy = text("sticker_recieved_by")
        val isActive = bool("is_active")
        val trainerUnits = integer("trainer_units")
        val unitsSinceLastExam = integer("add_units_since_last_exam") // TODO: Pls confirm

        fun fromRow(row: ResultRow, getLastExamDate: (Int) -> LocalDate?) = Member(
            id = row[id].value,
            surname = row[surname],
            prename = row[prename],
            group = row[group],
            level = row[level],
            total = row[total],
            birthday = row[birthday],
            lastExamDate = getLastExamDate(row[id].value),
            isTrainer = row[isTrainer],
            stickerAnimal = row[stickerAnimal],
            receivedStickerNumber = row[stickerReceived],
            stickerDateReceived = row[stickerDateReceived],
            stickerReceivedBy = row[stickerReceivedBy],
            isActive = row[isActive],
            trainerUnits = row[trainerUnits],
            unitsSinceLastExam = row[unitsSinceLastExam]
        )

    }

    fun <T: Any> upsertInto(insert: UpdateBuilder<T>) {
        insert[Member.surname] = surname
        insert[Member.prename] = prename
        insert[Member.group] = group
        insert[Member.level] = level
        insert[Member.total] = total
        insert[Member.birthday] = birthday
        if (lastExamDate != null) insert[Member.lastExamDate] = lastExamDate
        insert[Member.isTrainer] = isTrainer
        if (stickerAnimal != null) insert[Member.stickerAnimal] = stickerAnimal
        insert[Member.stickerReceived] = receivedStickerNumber
        if (stickerDateReceived != null) insert[Member.stickerDateReceived] = stickerDateReceived
        if (stickerReceivedBy != null) insert[Member.stickerReceivedBy] = stickerReceivedBy
        insert[Member.isActive] = isActive
        insert[Member.trainerUnits] = trainerUnits
        insert[Member.unitsSinceLastExam] = unitsSinceLastExam
    }
}

data class MemberWithIdAndString( // TODO: Kernsanierung
    val id: Int,
    val sticker_recieved_by: String?,
)