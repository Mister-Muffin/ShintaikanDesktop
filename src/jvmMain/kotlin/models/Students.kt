package models

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
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
    val sticker_units = integer("sticker_units")
    val sticker_animal = text("sticker_animal")
    val sticker_recieved = bool("sticker_recieved")
    val sticker_date_recieved = date("sticker_date_recieved")
    val sticker_recieved_by = integer("sticker_recieved_by")
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
    val sticker_units: Int,
    val sticker_animal: String?,
    val sticker_recieved: Boolean,
    val sticker_date_recieved: LocalDate?,
    val sticker_recieved_by: Int?,
    val radioClicked: Boolean = false // for sticker dialog (all radio buttons must be clicked before button activated)
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
                sticker_units = it[StudentTable.sticker_units],
                sticker_animal = it[StudentTable.sticker_animal],
                sticker_recieved = it[StudentTable.sticker_recieved],
                sticker_date_recieved = it[StudentTable.sticker_date_recieved],
                sticker_recieved_by = it[StudentTable.sticker_recieved_by]
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
            it[StudentTable.sticker_units] = student.sticker_units
            it[StudentTable.sticker_animal] = student.sticker_animal!!
            it[StudentTable.sticker_date_recieved] = LocalDate.now()
        }
    }
}