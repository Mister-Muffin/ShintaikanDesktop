package model

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.time.LocalDate

data class Participation(
    val id: Int,
    val userIds: String, // Pls help me, wtf
    val userIdsExam: String, // AAAAAaaaahhhhhh
    val date: LocalDate
) {
    // TODO: data as primary key, remove id, rename
    companion object : IntIdTable("teilnahme") {
        val userIds = text("user_ids")
        val userIdsExam = text("user_ids_exam")
        val date = date("date")

        fun fromRow(row: ResultRow) =
            Participation(row[id].value, row[userIds] ?: "", row[userIdsExam] ?: "", row[date])
    }

    fun <T : Any> updateInto(update: UpdateBuilder<T>) {
        update[Participation.id] = id
        update[Participation.userIds] = userIds
        update[Participation.userIdsExam] = userIdsExam
        update[Participation.date] = date
    }

    fun <T : Any> insertInto(insert: UpdateBuilder<T>) {
        insert[Participation.userIds] = userIds
        insert[Participation.userIdsExam] = userIdsExam
        insert[Participation.date] = date
    }

}
