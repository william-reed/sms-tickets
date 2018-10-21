package xyz.williamreed.smstickets.models.command

abstract class Command(val name: String, val helpText: String) {
    private val children = arrayListOf<Command>()

    fun addChild(cmd: Command) = children.add(cmd)
}

open class NoArgumentCommand(name: String, helpText: String, val optional: Boolean = false) : Command(name, helpText)

open class ArgumentCommand(name: String, helpText: String, private val argumentPattern: Regex, optional: Boolean = false) :
        NoArgumentCommand(name, helpText, optional) {
    
    fun matches(value: String) = argumentPattern.matches(value)
}

class DefaultArgumentCommand(name: String, helpText: String, argumentPattern: Regex, private val defaultArgument: String,
                             optional: Boolean = false) :
        ArgumentCommand(name, helpText, argumentPattern, optional)