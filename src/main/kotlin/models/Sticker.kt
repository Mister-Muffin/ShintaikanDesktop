package models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object StickerTable : Table("sticker") {
    val id = integer("id").autoIncrement()
    val m_id = integer("m_id")
    val unit = integer("unit")
    val date_recieved = date("date_recieved")
    val recieved_by = integer("recieved_by")
    // val sticker_animal = text("sticker_animal")
}

data class Sticker(
    val id: Int,
    val mId: Int,
    val unit: Int,
    val dateRecieved: LocalDate?,
    val recievedBy: String?,
    //val sticker_animal: String?,
)

fun createStickerTable() {
    return transaction {
        if (StickerTable.exists()) {
            println("Skip sticker table")
            return@transaction
        }
        StickerTable.createStatement().forEach { statement -> exec(statement) }
    }
}

fun importSticker(member: Member) {
    val stickerHistoryList = member.sticker_recieved_by.toString().trim(',').split(",")
    return transaction {
        if (StickerTable.exists()) {
            println("skip import")
            return@transaction
        }
        stickerHistoryList.forEach { stickerHistoryPart ->
            val singleStats = stickerHistoryPart.split(":")
            val stickerUnit: Int = singleStats[0].toInt()
            val stickerBy: Int = singleStats[1].toInt()

            val stickerDate = singleStats[2]
            val stickerDateFormatted = LocalDate.parse(stickerDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

            StickerTable.insertIgnore {
                it[m_id] = member.id
                it[unit] = stickerUnit
                it[date_recieved] = LocalDate.parse(stickerDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                it[recieved_by] = stickerBy
            }
            println("Import ${member.id}")
        }
    }
}

fun removeStickerColumns() {
    return transaction {
        MemberTable.sticker_recieved_by.dropStatement().forEach { statement -> exec(statement) }
        MemberTable.sticker_date_recieved.dropStatement().forEach { statement -> exec(statement) }
    }
}
