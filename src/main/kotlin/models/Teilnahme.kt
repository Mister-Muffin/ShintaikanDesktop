package models

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

object TeilnahmeTable : Table("teilnahme") {
    val id = integer("id").autoIncrement()
    val userId = text("user_ids")
    val userIdExam = text("user_ids_exam")
    val date = date("date")
}

data class Teilnahme(
    val id: Int,
    val userIds: String?,
    val userIdsExam: String?,
    val date: LocalDate
)

fun loadTeilnahme(): List<Teilnahme> {

    return transaction {
        TeilnahmeTable.selectAll().map {
            Teilnahme(
                id = it[TeilnahmeTable.id],
                userIds = it[TeilnahmeTable.userId],
                userIdsExam = it[TeilnahmeTable.userIdExam],
                date = it[TeilnahmeTable.date]
            )
        }
    }
}

fun loadExams(): List<Teilnahme> {

    return transaction {
        TeilnahmeTable.select(TeilnahmeTable.userIdExam.isNotNull()).map {
            Teilnahme(
                id = it[TeilnahmeTable.id],
                userIds = it[TeilnahmeTable.userId],
                userIdsExam = it[TeilnahmeTable.userIdExam],
                date = it[TeilnahmeTable.date]
            )
        }
    }
}

suspend fun insertTeilnahme(ids: String, isExam: Boolean) {
    val today = suspendedTransactionAsync {
        TeilnahmeTable.select(where = TeilnahmeTable.date eq LocalDate.now()).map {
            Teilnahme(
                id = it[TeilnahmeTable.id],
                userIds = it[TeilnahmeTable.userId],
                userIdsExam = it[TeilnahmeTable.userIdExam],
                date = it[TeilnahmeTable.date]
            )
        }
    }.await()
    if (today.size == 0) {
        suspendedTransactionAsync {
            TeilnahmeTable.insert {
                if (isExam) it[userIdExam] = ids else it[userId] = ids
                it[date] = LocalDate.now()
            }
        }.await()
    } else {
        suspendedTransactionAsync {
            if (isExam) {
                TeilnahmeTable.update(where = { TeilnahmeTable.date eq LocalDate.now() }) {
                    it[userIdExam] =
                        "${today[0].userIdsExam?.trim { it <= ',' }}, ${ids.trim { it <= ',' }}".trim { it <= ',' }
                            .filter { !it.isWhitespace() }
                }
            } else {
                TeilnahmeTable.update(where = { TeilnahmeTable.date eq LocalDate.now() }) {
                    it[userId] = "${today[0].userIds?.trim { it <= ',' }}, ${ids.trim { it <= ',' }}".trim { it <= ',' }
                        .filter { !it.isWhitespace() }
                }
            }
        }.await()
    }
}

fun setAddUnitsSinceLastExam(member: Member) {
    transaction {
        MemberTable.update(where = { MemberTable.id eq member.id }) {
            it[add_units_since_last_exam] = 0
        }
    }
}
