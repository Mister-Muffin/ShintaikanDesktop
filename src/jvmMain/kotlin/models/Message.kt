package models

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

object MessageTable : Table("messages") {
    val id = integer("id").autoIncrement()
    val text = text("message")
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
                message = it[MessageTable.text],
                short = it[MessageTable.short],
                dateCreated = it[MessageTable.dateCreated]
            )
        }
    }

    //print(ehre)
}

fun addMessage(message: Message): Int {
    transaction {
        MessageTable.insert {
            it[text] = message.message
            it[short] = ""
            it[dateCreated] = LocalDate.now()
        }
    }
    var id = -1
    transaction {
        MessageTable.select(MessageTable.text eq message.message and (MessageTable.dateCreated eq MessageTable.dateCreated))
            .map {
                id = it[MessageTable.id].toInt()
            }
    }
    return id
}

fun deleteMessage(id: Int) {
    return transaction {
        MessageTable.deleteWhere { MessageTable.id eq id }
    }
}