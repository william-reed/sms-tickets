package xyz.williamreed.smstickets.models.command

import java.lang.IllegalArgumentException
import java.lang.UnsupportedOperationException
import kotlin.reflect.KClass

abstract class Command(val name: String, val helpText: String, val valueType: KClass<*>) {
    private val children = arrayListOf<Command>()

    fun addChild(cmd: Command) = children.add(cmd)
    abstract fun validate(text: String): Boolean

    fun <T : Any> parse(input: String): T {
        val parsed = parseToString(input)

        // not sure whats a better way to handle these conversions.
        return when (valueType) {
            String::class -> parsed as T
            Int::class -> parsed.toInt() as T
            Double::class -> parsed.toDouble() as T
            Float::class -> parsed.toFloat() as T
            Boolean::class -> parsed.toBoolean() as T
            // TOOD: allow for time / date parsing
            else -> {
                throw UnsupportedOperationException()
            }
        }
    }

    protected abstract fun parseToString(input: String): String

}

// no arg command so conversion function returns false - it shouldn't matter?
// what other way can I omit the parse implementation?
open class NoArgumentCommand(name: String, helpText: String, val optional: Boolean = false) :
        Command(name, helpText, Any::class) {
    override fun validate(text: String) = text == name
    override fun parseToString(input: String) = ""
}

open class ArgumentCommand(name: String, helpText: String, valueType: KClass<*>, val optional: Boolean = false,
                           protected val argumentPattern: Regex) :
        Command(name, helpText, valueType) {

    override fun validate(text: String): Boolean {
        if (!text.startsWith("$name "))
            return false
        val values = text.replaceFirst("$name ", "")
        return argumentPattern.matches(values)
    }

    override fun parseToString(input: String): String {
        if (!validate(input))
            throw IllegalArgumentException("text not validated. Screen this method with #validate first")

        return input.replaceFirst("$name ", "")
    }
}

class DefaultArgumentCommand(name: String, helpText: String, valueType: KClass<*>, optional: Boolean = false,
                             argumentPattern: Regex, private val defaultArgument: String) :
        ArgumentCommand(name, helpText, valueType, optional, argumentPattern) {

    override fun validate(text: String): Boolean {
        if (!text.startsWith(name)) return false
        var values = text.replaceFirst(name, "")
        if (values.isEmpty()) return true
        values = values.replaceFirst(" ", "")
        return argumentPattern.matches(values)
    }

    override fun parseToString(input: String): String {
        if (!validate(input))
            throw IllegalArgumentException("text not validated. Screen this method with #validate first")

        val values = input.replaceFirst(name, "").replaceFirst(" ", "")
        if (values.isEmpty()) return defaultArgument
        return values
    }
}