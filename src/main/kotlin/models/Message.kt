package models

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

object MessageTable : Table("messages") {
    val id = integer("id").autoIncrement()
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

suspend fun loadMessages(): List<Message> {

    return suspendedTransactionAsync(Dispatchers.IO) {
        MessageTable.selectAll().sortedByDescending { it[MessageTable.dateCreated] }.map {
            Message(
                id = it[MessageTable.id],
                message = it[MessageTable.message],
                short = it[MessageTable.short],
                dateCreated = it[MessageTable.dateCreated]
            )
        }
    }.await()
}

fun addMessage(message: Message): Int {
    transaction {
        MessageTable.insert {
            it[this.message] = message.message
            it[short] = ""
            it[dateCreated] = LocalDate.now()
        }
    }
    var id = -1
    transaction {
        MessageTable.select(MessageTable.message eq message.message and (MessageTable.dateCreated eq MessageTable.dateCreated))
            .map {
                id = it[MessageTable.id].toInt()
            }
    }
    return id
}

fun editMessage(message: Message) {
    transaction {
        MessageTable.update(where = { MessageTable.id eq message.id }) {
            it[MessageTable.message] = message.message
        }
    }
}

fun deleteMessage(id: Int) {
    return transaction {
        MessageTable.deleteWhere { MessageTable.id eq id }
    }
}