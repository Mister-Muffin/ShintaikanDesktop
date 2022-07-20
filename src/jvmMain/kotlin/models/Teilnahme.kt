package models

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

object TeilnahmeTable : Table("test") {
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
        val newRow = transaction {
            TeilnahmeTable.insert {
                it[TeilnahmeTable.userId] = ids
                it[TeilnahmeTable.date] = LocalDate.now()
            }
        }
        newRow
    } else {
        val updatedRow = transaction {
            TeilnahmeTable.update(where = { TeilnahmeTable.date eq LocalDate.now() }) {
                with(SqlExpressionBuilder) {
                    it[TeilnahmeTable.userId] =
                        "${today[0].userId.trim { it <= ',' }}, ${ids.trim { it <= ',' }}".trim { it <= ',' }
                            .filter { !it.isWhitespace() }
                }
            }
        }
        updatedRow
    }
}