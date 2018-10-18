package xyz.williamreed.smstickets.models.tables

import org.jetbrains.exposed.dao.IntIdTable
import xyz.williamreed.smstickets.models.Carrier

object User : IntIdTable() {
    val firstName = varchar("first_name", 32)
    val lastName = varchar("last_name", 32)
    val phone = varchar("phone", 10)
    val carrier = enumeration("carrier", Carrier::class.java)
    val group = reference("group", Group)
}