package models

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
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
    val is_trainer: Boolean
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
                is_trainer = it[StudentTable.is_trainer]
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