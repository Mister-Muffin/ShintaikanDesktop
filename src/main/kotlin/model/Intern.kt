package model

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder

data class Intern(
    val dbVersion: Int = 0,
) {
    companion object : Table("intern") {
        val dbVersion = integer("db_version").default(0)

        fun fromRow(row: ResultRow) = Intern(row[dbVersion])
    }

    fun <T : Any> updateInto(insert: UpdateBuilder<T>) {
        insert[Intern.dbVersion] = dbVersion
    }
}
