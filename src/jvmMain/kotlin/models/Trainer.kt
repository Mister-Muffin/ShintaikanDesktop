package models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object TrainerTable : Table("trainer") {
    val id = integer("id")
    val name = text("name")
    val onlyCotrainer = bool("only_co_trainer")
}

data class Trainer(val name: String, val id: Int, val onlyCotrainer: Boolean)

fun loadTrainers(): List<Trainer> {

    return transaction {
        TrainerTable.selectAll().map {
            Trainer(
                name = it[TrainerTable.name],
                id = it[TrainerTable.id],
                onlyCotrainer = it[TrainerTable.onlyCotrainer]
            )
        }
    }
}