package models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

object MessageTable : Table("messages") {
    val id = integer("id")
    val message = text("message")
    val short = text("short")
    val dateCreated = date("date_created")
}

data class Message(
    val id: Int,
    val message: String,
    val short: String,
    val dateCreated: LocalDate
)

fun loadMessages(): List<Message> {

    return transaction {
        MessageTable.selectAll().sortedByDescending { it[MessageTable.dateCreated] }.map {
            Message(
                id = it[MessageTable.id],
                message = it[MessageTable.message],
                short = it[MessageTable.short],
                dateCreated = it[MessageTable.dateCreated]
            )
        }
    }

    //print(ehre)
}