package xyz.williamreed.smstickets.models.tables

import org.jetbrains.exposed.dao.IntIdTable

object Group : IntIdTable() {
    val name = varchar("name", 128)
}