package model

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.time.LocalDate

data class Participation(
    val id: Int,
    val memberId: Int,
    val date: LocalDate,
    val note: String,
    val exam: Boolean
) {
    companion object : IntIdTable("anwesenheit") {
        val memberId = integer("member_id")
        val date = date("date")
        val note = text("note")
        val exam = bool("exam")

        fun fromRow(row: ResultRow) =
            Participation(row[id].value, row[memberId], row[date], row[note], row[exam])
    }

    fun <T : Any> updateInto(update: UpdateBuilder<T>) {
        update[Participation.id] = id
        update[Participation.memberId] = memberId
        update[Participation.date] = date
        update[Participation.note] = note
        update[Participation.exam] = exam
    }

    fun <T : Any> insertInto(insert: UpdateBuilder<T>) {
        insert[Participation.memberId] = memberId
        insert[Participation.date] = date
        insert[Participation.note] = note
        insert[Participation.exam] = exam
    }

}

@Deprecated("")
data class OldParticipation(
    val id: Int,
    val userIds: String,
    val userIdsExam: String,
    val date: LocalDate
) {
    companion object : IntIdTable("teilnahme") {
        val userIds = text("user_ids")
        val userIdsExam = text("user_ids_exam")
        val date = date("date")

        fun fromRow(row: ResultRow) =
            OldParticipation(row[id].value, row[userIds] ?: "", row[userIdsExam] ?: "", row[date])
    }

    /*fun <T : Any> updateInto(update: UpdateBuilder<T>) {
        update[OldParticipation.id] = id
        update[OldParticipation.userIds] = userIds
        update[OldParticipation.userIdsExam] = userIdsExam
        update[OldParticipation.date] = date
    }

    fun <T : Any> insertInto(insert: UpdateBuilder<T>) {
        insert[OldParticipation.userIds] = userIds
        insert[OldParticipation.userIdsExam] = userIdsExam
        insert[OldParticipation.date] = date
    }*/
}
