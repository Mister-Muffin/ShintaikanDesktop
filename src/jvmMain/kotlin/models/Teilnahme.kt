package models

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

object TeilnahmeTable : Table("teilnahme") {
    val id = integer("id").autoIncrement()
    val userId = text("user_ids")
    val date = date("date")
}

data class Teilnahme(
    val id: Int,
    val userId: String,
    val date: LocalDate
)

fun loadTeilnahme(): List<Teilnahme> {

    return transaction {
        TeilnahmeTable.selectAll().map {
            Teilnahme(
                id = it[TeilnahmeTable.id],
                userId = it[TeilnahmeTable.userId],
                date = it[TeilnahmeTable.date]
            )
        }
    }

    //print(ehre)
}

fun insertTeilnahme(ids: String) {
    val today = transaction {
        TeilnahmeTable.select(where = TeilnahmeTable.date eq LocalDate.now()).map {
            Teilnahme(
                id = it[TeilnahmeTable.id],
                userId = it[TeilnahmeTable.userId],
                date = it[TeilnahmeTable.date]
            )
        }
    }
    if (today.size == 0) {
        transaction {
            TeilnahmeTable.insert {
                it[userId] = ids
                it[date] = LocalDate.now()
            }
        }
    } else {
        transaction {
            TeilnahmeTable.update(where = { TeilnahmeTable.date eq LocalDate.now() }) {
                it[userId] = "${today[0].userId.trim { it <= ',' }}, ${ids.trim { it <= ',' }}".trim { it <= ',' }
                    .filter { !it.isWhitespace() }
            }
        }
    }
}