package xyz.williamreed.smstickets.models.tables

import org.jetbrains.exposed.dao.IntIdTable
import java.util.concurrent.TimeUnit

object RepeatingTask : IntIdTable() {
    val task = reference("task", Task)
    val period = integer("period")
    val timeUnit = enumeration("timeUnit", TimeUnit::class.java)
}