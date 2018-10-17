package xyz.williamreed.sms.tickets.models

import org.jetbrains.exposed.dao.IntIdTable

object Group : IntIdTable() {
    val name = varchar("name", 128)
}