package xyz.williamreed.smstickets.models.tables

import org.jetbrains.exposed.dao.IntIdTable

object Task : IntIdTable() {
    val description = varchar("description", 1024)
    val assignee = reference("assignee", User).nullable()
    // assign this ticket randomly?
    val random = bool("random").default(false)

}