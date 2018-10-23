package xyz.williamreed.smstickets

import xyz.williamreed.smstickets.models.command.Command
import xyz.williamreed.smstickets.models.command.command

var commands = listOf(
        command {
            name = "create task"
            helpText = "Create a task: `create task <task description>`"
        },
        command {
            name = "view task"
            helpText = "View a task: `view task <task number>`"
            argumentPattern = "\\d+"
        },
        command {
            name = "view tasks"
            helpText = "View all tasks in this group"
            noArgs = true
        },
        command {
            name = "view my tasks"
            helpText = "View all your tasks in this group"
            noArgs = true
        },
        command {
            name = "delete task"
            helpText = "Delete a task: `delete task <task number>`"
            argumentPattern = "\\d+"
        }
)