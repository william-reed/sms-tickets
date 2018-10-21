package xyz.williamreed.smstickets.models.command

fun command(block: CommandBuilder.() -> Unit) = CommandBuilder().apply(block).build()

class CommandBuilder {
    var name = ""
    var optional = false
    var argumentPattern = ".*"
    var noArgs = false
    var helpText = ""
    var defaultArgument = ""
    private val children = arrayListOf<Command>()

    fun child(block: CommandBuilder.() -> Unit) {
        children.add(CommandBuilder().apply(block).build())
    }

    fun build(): Command {
        if (noArgs)
            return buildNoArgCmd()
        else
            return buildArgCmd()
    }

    private fun buildNoArgCmd(): NoArgumentCommand {
        val cmd = NoArgumentCommand(name, helpText, optional)
        children.forEach { cmd.addChild(it) }
        return cmd
    }

    private fun buildArgCmd(): ArgumentCommand {
        val cmd = if (defaultArgument !== "")
            DefaultArgumentCommand(name, helpText, Regex(argumentPattern), defaultArgument, optional)
        else
            ArgumentCommand(name, helpText, Regex(argumentPattern), optional)
        children.forEach { cmd.addChild(it) }
        return cmd
    }
}

// example:
//val cmd = command {
//    name = "ping"
//    argumentPattern = "number match for IP..."
//    helpText = "send an ICMP request"
//
//    child {
//        name = "-n"
//        argumentPattern = "\\d"
//        optional = true
//        helpText = "how many pings do you want to send?"
//    }
//
//    child {
//        name = "-f"
//        optional = true
//        noArgs = true
//        helpText = "force stop on failure"
//    }
//}