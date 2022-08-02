package models

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

object StudentTable : Table("main") {
    val id = integer("id")
    val surname = text("surname")
    val prename = text("prename")
    val group = text("group")
    val level = text("level")
    val sum_years = text("sum_years")
    val total = integer("total")
    val birthday = date("birthday")
    val date_last_exam = date("date_last_exam")
    val is_trainer = bool("is_trainer")
    val sticker_animal = text("sticker_animal")
    val sticker_recieved = integer("sticker_recieved")
    val sticker_date_recieved = date("sticker_date_recieved")
    val sticker_recieved_by = integer("sticker_recieved_by")
    val is_active = bool("is_active")
}

data class Student(
    val id: Int,
    val surname: String,
    val prename: String,
    val group: String,
    val level: String,
    val sum_years: String,
    val total: Int,
    val birthday: LocalDate?,
    val date_last_exam: LocalDate?,
    val is_trainer: Boolean,
    val sticker_animal: String?,
    val sticker_recieved: Int,
    val sticker_date_recieved: LocalDate?,
    val sticker_recieved_by: Int?,
    val is_active: Boolean,
    val radioClicked: Boolean = false, // for sticker dialog (all radio buttons must be clicked before button activated)
    val stickerRecieved: Boolean = false, // for sticker dialog, if radio button is checked or not
    val sticker_show_again: Boolean = false, // for sticker dialog, if student is still missing stickers and the dialog should open again with this student
)

data class Trainer(
    val id: Int,
    val surname: String,
    val prename: String,
    val is_trainer: Boolean
)

fun loadStudents(): List<Student> {
    return transaction {
        StudentTable.selectAll().sortedByDescending { it[StudentTable.level] }.map {
            Student(
                id = it[StudentTable.id],
                surname = it[StudentTable.surname],
                prename = it[StudentTable.prename],
                group = it[StudentTable.group],
                level = it[StudentTable.level],
                sum_years = it[StudentTable.sum_years],
                total = it[StudentTable.total],
                birthday = it[StudentTable.birthday],
                date_last_exam = it[StudentTable.date_last_exam],
                is_trainer = it[StudentTable.is_trainer],
                sticker_animal = it[StudentTable.sticker_animal],
                sticker_recieved = it[StudentTable.sticker_recieved],
                sticker_date_recieved = it[StudentTable.sticker_date_recieved],
                sticker_recieved_by = it[StudentTable.sticker_recieved_by],
                is_active = it[StudentTable.is_active]
            )
        }
    }
}

fun loadTrainers(): List<Trainer> {

    return transaction {
        StudentTable.select(where = StudentTable.is_trainer eq true).map {
            Trainer(
                id = it[StudentTable.id],
                surname = it[StudentTable.surname],
                prename = it[StudentTable.prename],
                is_trainer = it[StudentTable.is_trainer]
            )
        }
    }
}

fun editIsTrainer(id: Int, is_trainer: Boolean) {
    return transaction {
        StudentTable.update(where = { StudentTable.id eq id }) {
            it[StudentTable.is_trainer] = is_trainer
        }
    }
}

fun editStudentSticker(student: Student) {
    return transaction {
        StudentTable.update(where = { StudentTable.id eq student.id }) {
            it[StudentTable.sticker_recieved] = student.sticker_recieved
            it[StudentTable.sticker_recieved_by] = student.sticker_recieved_by!!
            it[StudentTable.sticker_animal] = student.sticker_animal!!
            it[StudentTable.sticker_date_recieved] = LocalDate.now()
        }
    }
}

fun deactivateStudent(prename: String, surname: String) {
    return transaction {
        StudentTable.update(
            where = { StudentTable.prename eq prename and (StudentTable.surname eq surname) }) {
            it[is_active] = false
        }
    }
}

/**
 * Renames a member by indentifing it with the old name
 * and renaming it with the new name
 *
 * @param oldName is a Pair where 'first' is the prename and 'second' is the surname
 * @param newName is a Pair where 'first' is the prename and 'second' is the surname
 */
fun renameStudent(oldName: Pair<String, String>, newName: Pair<String, String>) {
    return transaction {
        StudentTable.update(
            where = { StudentTable.prename eq oldName.first and (StudentTable.surname eq oldName.second) }) {
            it[prename] = newName.first
            it[surname] = newName.second
        }
    }
}