package models

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction
import removeMultipleWhitespaces
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

object MemberTable : Table("main") {
    val id = integer("id").autoIncrement()
    val surname = text("surname")
    val prename = text("prename")
    val group = text("group")
    val level = text("level")
    val total = integer("total")
    val birthday = date("birthday")
    val date_last_exam = date("date_last_exam")
    val is_trainer = bool("is_trainer")
    val sticker_animal = text("sticker_animal")
    val sticker_recieved = integer("sticker_recieved")
    val sticker_date_recieved = date("sticker_date_recieved")
    val sticker_recieved_by = text("sticker_recieved_by")
    val is_active = bool("is_active")
    val trainer_units = integer("trainer_units")
    val add_units_since_last_exam = integer("add_units_since_last_exam")
}

data class Member(
    val id: Int,
    val surname: String,
    val prename: String,
    val group: String,
    val level: String,
    val total: Int,
    val birthday: LocalDate,
    val date_last_exam: LocalDate?,
    val is_trainer: Boolean,
    val sticker_animal: String?,
    val sticker_recieved: Int,
    val sticker_date_recieved: LocalDate?,
    val sticker_recieved_by: String?,
    val is_active: Boolean,
    val trainer_units: Int,
    val add_units_since_last_exam: Int,
    val radioClicked: Boolean = false, // for sticker dialog (all radio buttons must be clicked before button activated)
    val stickerRecieved: Boolean = false, // for sticker dialog, if radio button is checked or not
    val sticker_show_again: Boolean = true, // for sticker dialog, if student is still missing stickers and the dialog should open again with this student
) {
    val age: Int = Period.between(birthday, LocalDate.now()).years

    /**
     * Example: z Kyu weiss -> Kyu weiss
     * @return The member's level formatted to be displayed in the UI
     */
    fun formattedLevel(): String {
        var level = this.level
        if (level.contains("Dan")) {
            level = level.drop(2) // drop the first two letters
        } else if (level == "z Kyu weiss") {
            level = level.drop(2)
        }
        return level
    }

}

data class MemberWithIdAndString(
    val id: Int,
    val sticker_recieved_by: String?,
)

data class Trainer(
    val id: Int,
    val surname: String,
    val prename: String,
    val is_trainer: Boolean,
    val trainer_units: Int
)

suspend fun loadMembers(hideInactive: Boolean = true): List<Member> {
    return suspendedTransactionAsync(Dispatchers.IO) {
        val members =
            if (hideInactive) MemberTable.select(where = { MemberTable.is_active eq true })
            else MemberTable.selectAll()

        members.sortedByDescending { it[MemberTable.level] }.map {
            Member(
                id = it[MemberTable.id],
                surname = it[MemberTable.surname],
                prename = it[MemberTable.prename],
                group = it[MemberTable.group],
                level = it[MemberTable.level],
                total = it[MemberTable.total],
                birthday = it[MemberTable.birthday],
                date_last_exam = getLastExamDate(it[MemberTable.id]),
                is_trainer = it[MemberTable.is_trainer],
                sticker_animal = it[MemberTable.sticker_animal],
                sticker_recieved = it[MemberTable.sticker_recieved],
                sticker_date_recieved = it[MemberTable.sticker_date_recieved],
                sticker_recieved_by = it[MemberTable.sticker_recieved_by],
                is_active = it[MemberTable.is_active],
                trainer_units = it[MemberTable.trainer_units],
                add_units_since_last_exam = it[MemberTable.add_units_since_last_exam]
            )
        }
    }.await()
}

fun loadTrainers(): List<Trainer> {
    return transaction {
        MemberTable.select(where = MemberTable.is_trainer eq true).map {
            Trainer(
                id = it[MemberTable.id],
                surname = it[MemberTable.surname],
                prename = it[MemberTable.prename],
                is_trainer = it[MemberTable.is_trainer],
                trainer_units = it[MemberTable.trainer_units]
            )
        }
    }
}

fun getLastExamDate(memberId: Int): LocalDate? {
    return transaction {
        MemberTable.join(TeilnahmeTable, JoinType.CROSS)
            .select(where = { TeilnahmeTable.userIdExam like "%${memberId}%" and (MemberTable.id eq memberId) })
            .orderBy(TeilnahmeTable.id, SortOrder.DESC).limit(1).map {
                mapOf("date" to it[TeilnahmeTable.date])
            }.let {
                if (it.isNotEmpty()) {
                    it.first()["date"]
                } else null
            }
    }
}

fun editIsTrainer(id: Int, is_trainer: Boolean) {
    return transaction {
        MemberTable.update(where = { MemberTable.id eq id }) {
            it[MemberTable.is_trainer] = is_trainer
        }
    }
}

fun increaseTrainerUnitCount(trainer: Trainer) {
    return transaction {
        MemberTable.update(where = { MemberTable.id eq trainer.id }) {
            with(SqlExpressionBuilder) {
                it.update(trainer_units, trainer_units + 1)
            }
        }
    }
}

@Suppress("RemoveRedundantQualifierName")
fun editMemberSticker(member: Member) {
    val currentStickerRecievedBy = transaction {
        MemberTable.slice(MemberTable.id, MemberTable.sticker_recieved_by).select(where = MemberTable.id eq member.id)
            .map {
                MemberWithIdAndString(
                    id = it[MemberTable.id], sticker_recieved_by = it[MemberTable.sticker_recieved_by]
                )
            }
    }[0]
    return transaction {
        MemberTable.update(where = { MemberTable.id eq member.id }) {
            it[MemberTable.sticker_recieved] = member.sticker_recieved
            it[MemberTable.sticker_recieved_by] =
                if (currentStickerRecievedBy.sticker_recieved_by.isNullOrEmpty()) "${member.sticker_recieved_by},"
                else "${currentStickerRecievedBy.sticker_recieved_by}${member.sticker_recieved_by},"
            //it[StudentTable.sticker_animal] = student.sticker_animal!!
            it[MemberTable.sticker_date_recieved] = LocalDate.now()
        }
    }
}

/**
 * Deactivate a member by indentifing it with its name
 *
 * @param name is a Pair where 'first' is the prename and 'second' is the surname
 */
suspend fun deactivateMember(name: Pair<String, String>): Int {
    return suspendedTransactionAsync(Dispatchers.IO) {
        MemberTable.update(where = { MemberTable.prename eq name.first and (MemberTable.surname eq name.second) }) {
            it[is_active] = false
        }
    }.await()
}

/**
 * Renames a member by indentifing it with the old name
 * and renaming it with the new name
 *
 * @param oldName is a Pair where 'first' is the prename and 'second' is the surname
 * @param newName is a Pair where 'first' is the prename and 'second' is the surname
 */
suspend fun renameMember(oldName: Pair<String, String>, newName: Pair<String, String>): Int {
    return suspendedTransactionAsync(Dispatchers.IO) {
        MemberTable.update(where = { MemberTable.prename eq oldName.first and (MemberTable.surname eq oldName.second) }) {
            it[prename] = newName.first
            it[surname] = newName.second
        }
    }.await()
}

/**
 * Updates or creates a member by indentifing it with its name
 *
 * @param name is a Pair where 'first' is the prename and 'second' is the surname
 * @param birthday is the Birthday to update as a string in format dd/MM/yyyy
 */
suspend fun updateMember(name: Pair<String, String>, group: String, level: String, birthday: String): Any {
    val memberExists = suspendedTransactionAsync(Dispatchers.IO) {
        MemberTable.select(where = { MemberTable.prename eq name.first and (MemberTable.surname eq name.second) })
            .toList()
    }.await().isNotEmpty()
    if (memberExists) {
        return suspendedTransactionAsync(Dispatchers.IO) {
            MemberTable.update(where = { MemberTable.prename eq name.first and (MemberTable.surname eq name.second) }) {
                it[MemberTable.group] = group
                it[MemberTable.level] = level.removeMultipleWhitespaces()
                it[MemberTable.is_active] = true
                if (birthday.split("/")[0].length == 1) {
                    if (birthday.split("/")[1].length == 1) {
                        it[MemberTable.birthday] = LocalDate.parse(birthday, DateTimeFormatter.ofPattern("M/d/yyyy"))
                    } else {
                        it[MemberTable.birthday] = LocalDate.parse(birthday, DateTimeFormatter.ofPattern("M/dd/yyyy"))
                    }
                } else {
                    if (birthday.split("/")[1].length == 1) {
                        it[MemberTable.birthday] = LocalDate.parse(birthday, DateTimeFormatter.ofPattern("MM/d/yyyy"))
                    } else {
                        it[MemberTable.birthday] = LocalDate.parse(birthday, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                    }
                }
            }
        }.await()
    } else {
        return suspendedTransactionAsync(Dispatchers.IO) {
            MemberTable.insert {
                it[MemberTable.prename] = name.first
                it[MemberTable.surname] = name.second
                it[MemberTable.group] = group
                it[MemberTable.level] = level.removeMultipleWhitespaces()
                if (birthday.split("/")[0].length == 1) {
                    if (birthday.split("/")[1].length == 1) {
                        it[MemberTable.birthday] = LocalDate.parse(birthday, DateTimeFormatter.ofPattern("M/d/yyyy"))
                    } else {
                        it[MemberTable.birthday] = LocalDate.parse(birthday, DateTimeFormatter.ofPattern("M/dd/yyyy"))
                    }
                } else {
                    if (birthday.split("/")[1].length == 1) {
                        it[MemberTable.birthday] = LocalDate.parse(birthday, DateTimeFormatter.ofPattern("MM/d/yyyy"))
                    } else {
                        it[MemberTable.birthday] = LocalDate.parse(birthday, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                    }
                }
            }
        }.await()
    }
}
