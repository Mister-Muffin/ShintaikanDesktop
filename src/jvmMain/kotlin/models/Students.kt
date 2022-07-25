package models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
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
}

data class Student(
    val id: Int,
    val surname: String,
    val prename: String,
    val group: String,
    val level: String,
    //val sessions: LocalTime,
    val sum_years: String,
    val total: Int,
    val birthday: LocalDate?
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
            )
        }
    }
}