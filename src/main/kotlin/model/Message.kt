package model

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.time.LocalDate

data class Message(
    val id: Int,
    var message: String,
    val short: String,
    val dateCreated: LocalDate
) {
    companion object : IntIdTable("messages") {
        val message = text("message")
        val short = text("short")
        val dateCreated = date("date_created")

        fun fromRow(row: ResultRow) = Message(row[id].value, row[message], row[short], row[dateCreated])
    }

    fun <T: Any> updateInto(insert: UpdateBuilder<T>) {
        insert[Message.message] = message
        insert[Message.short] = short
        insert[Message.dateCreated] = dateCreated
    }
}
