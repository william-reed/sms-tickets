package xyz.williamreed.smstickets.models.command

import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import kotlin.reflect.KClass

/***
 *      ___       _             __
 *     |_ _|_ __ | |_ ___ _ __ / _| __ _  ___ ___  ___
 *      | || '_ \| __/ _ \ '__| |_ / _` |/ __/ _ \/ __|
 *      | || | | | ||  __/ |  |  _| (_| | (_|  __/\__ \
 *     |___|_| |_|\__\___|_|  |_|  \__,_|\___\___||___/
 *
 */

/**
 * Regular expression to wrap
 */
const val lookAroundWhiteSpace = "(?<=\\s|^)%s(?=\\s|\$)"

/**
 * A Command
 *
 * represents a command in any form, from a shell command to a more human readable form.
 */
interface Command {
    /** the command name, in the ping example `ping` would be the name **/
    val name: String

    /** the help text for this command */
    val helpText: String

    /**
     * validate the input
     * make sure the command contains the name of this command. Subclasses should override this
     */
    fun validate(input: String) = input.contains(Regex(lookAroundWhiteSpace.format(Regex.escape(name))))

    /**
     * parse the input
     * turn the input string into a map of key values mapping argument name to its value
     */
    fun parse(input: String, values: MutableMap<String, String>): MutableMap<String, String>

    /**
     * strip the input of its command
     * Remove each command from the input. E.g. calling strip on `ping 192.168.1.1` would return
     * ` `. Subclasses should override this.
     */
    fun strip(input: String) = input.replace(Regex(lookAroundWhiteSpace.format(Regex.escape(name))), "")
}

/**
 * An Argument based Command
 *
 * Sometimes commands are more than just a name. Sometimes they need arguments too. e.g. in
 * `ping -c 3 192.168.1.1`, `-c 3` would be an argument command
 */
interface ArgumentCommand : Command {
    /** is this argument command optional? */
    val optional: Boolean

    /** validate the input. Is the command name present in the input or is it optional? Sub classes should override */
    override fun validate(input: String) = super.validate(input) || optional
}

interface RootCommand : Command {
    val children: ArrayList<ArgumentCommand>

    override fun validate(input: String) = super.validate(input) && children.all { it.validate(input) }

    override fun parse(input: String, values: MutableMap<String, String>): MutableMap<String, String> {
        if (!validate(input)) throw IllegalArgumentException("Input not validated")
        return children.fold(values) { acc, argumentCommand -> argumentCommand.parse(input, acc) }
    }

    fun parse(input: String) = parse(input, mutableMapOf())

    override fun strip(input: String) = children.fold(super.strip(input)) { remainingInput, argumentCommand -> argumentCommand.strip(remainingInput) }

    fun addCommand(cmd: ArgumentCommand) {
        if (children.any { it.name == cmd.name }) throw IllegalArgumentException("Command with given name already exists in this root command.")
        children.add(cmd)
    }

    fun helpText() = StringBuilder().apply {
        append("$name: ")
        append(helpText)

        if (children.size > 0)
            appendln()

        children.forEachIndexed { index, child ->
            append(child.name + ": ")
            append(child.helpText)
            if (index + 1 != children.size) appendln()
        }
    }.toString()
}

/**
 * A Value Command
 *
 * Used to represent a command that takes in a value along with the command. E.g. `ping 192.168.1.1`,
 * the IP address is the value.
 */
interface ValueCommand : Command {
    /** The type of the value. not currently used */
    val type: KClass<*>
    /** the regular expression to match the value on */
    val pattern: Regex

    /**
     * Strip the input.
     * Remove the command name and values from the input string and return it
     * Subclasses will likely need to override and use this function in addition
     * @throws IllegalArgumentException if the input is not validated
     */
    override fun strip(input: String): String {
        // TODO: need some way to cache validation so I'm not doing it everywhere
        // or might not need validation here hmm
        if (!validate(input)) throw IllegalArgumentException("Input not validated")

        val beforeName = input.substringBefore(name)
        val afterName = input.substringAfter(name).replaceFirst(pattern, "")

        return beforeName + afterName
    }
}

/***
 *      ___                 _                           _        _   _
 *     |_ _|_ __ ___  _ __ | | ___ _ __ ___   ___ _ __ | |_ __ _| |_(_) ___  _ __  ___
 *      | || '_ ` _ \| '_ \| |/ _ \ '_ ` _ \ / _ \ '_ \| __/ _` | __| |/ _ \| '_ \/ __|
 *      | || | | | | | |_) | |  __/ | | | | |  __/ | | | || (_| | |_| | (_) | | | \__ \
 *     |___|_| |_| |_| .__/|_|\___|_| |_| |_|\___|_| |_|\__\__,_|\__|_|\___/|_| |_|___/
 *                   |_|
 */
class NoValueRootCommand(override val name: String,
                         override val helpText: String) :
        RootCommand {
    override val children = arrayListOf<ArgumentCommand>()
}

class ValueRootCommand(override val name: String,
                       override val helpText: String,
                       override val type: KClass<*>,
                       override val pattern: Regex) :
        ValueCommand, RootCommand {
    override val children = arrayListOf<ArgumentCommand>()

    override fun validate(input: String) = super<RootCommand>.validate(input) && pattern.containsMatchIn(input.substringAfter(name))

    override fun parse(input: String, values: MutableMap<String, String>): MutableMap<String, String> {
        if (!validate(input)) throw IllegalArgumentException("Input not validated")
        val match = pattern.find(input.substringAfter(name))?.value
                ?: throw IllegalArgumentException("Input not validated")
        return super.parse(input, values.apply { this[name] = match })
    }

    override fun strip(input: String) = super<RootCommand>.strip(super<ValueCommand>.strip(input))
}

/**
 * An Argument Command that needs no values
 *
 * Usually used to represent a flag in a command. E.g. in `ping -A 192.168.1.1`, `-A` would be
 * represented by this class.
 */
class NoValueArgumentCommand(override val name: String,
                             override val helpText: String) :
        ArgumentCommand {
    /** it never makes sense to have a mandatory NoValueArgumentCommand - if you do its the same as default behavior. */
    override val optional = true

    /** Validate the input. Since optional is always true, this will always validate, returning true */
    override fun validate(input: String) = true

    /**
     * Parse the input.
     * There are no values to parse so just return the input map
     * @throws IllegalArgumentException if the input is not validated
     */
    override fun parse(input: String, values: MutableMap<String, String>): MutableMap<String, String> {
        // probably don't need with current implementation, but can't hurt to leave
        if (!validate(input)) throw IllegalArgumentException("Input not validated")
        return values
    }
}

/**
 * A value based argument command.
 *
 * Often times arguments to a command require a value. e.g. `ping -c 3 192.168.1.1`, `-c 3` would
 * be an argument command that requires a value as well.
 */
class ValueArgumentCommand(override val name: String,
                           override val helpText: String,
                           override val type: KClass<*>,
                           override val pattern: Regex,
                           override val optional: Boolean = false) :
        ValueCommand, ArgumentCommand {

    /**
     * Validate the input
     *
     * Is the command present if it is not optional? If so make sure the given pattern is matched
     * right after the command name
     */
    override fun validate(input: String) = (super<ValueCommand>.validate(input) &&
            // regex is start of line, optional white space, match pattern, match word boundary, optional characters
            input.substringAfter(name).matches(Regex("^\\s*${pattern.pattern}\\b.*"))) || optional

    /**
     * Parse the input
     * Put the value of this ArgumentCommand into the map as a map of command name to value.
     * @throws IllegalArgumentException if the input is not validated
     */
    override fun parse(input: String, values: MutableMap<String, String>): MutableMap<String, String> {
        if (!validate(input)) throw IllegalArgumentException("Input not validated")
        val match = pattern.find(input.substringAfter(name))?.value ?: return values
        return values.apply {
            this[name] = match
        }
    }
}