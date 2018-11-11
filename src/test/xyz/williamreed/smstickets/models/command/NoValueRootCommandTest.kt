package xyz.williamreed.smstickets.models.command

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.lang.IllegalArgumentException

class NoValueRootCommandTest {

    @Test
    fun `helpText generates properly with just NoValueRootCommand`() {
        val rootCommand = NoValueRootCommand("ping", "send an ICMP request")
        assertThat(rootCommand.helpText()).isEqualTo("ping: send an ICMP request")
    }

    @Test
    fun `helpText generates properly with just one child`() {
        val rootCommand = NoValueRootCommand("ping", "send an ICMP request")
        rootCommand.addCommand(ValueArgumentCommand("-c", "how many to send", Int::class, Regex("\\d")))
        assertThat(rootCommand.helpText()).isEqualTo("ping: send an ICMP request\n-c: how many to send")
    }

    @Test
    fun `helpText generates properly with just multiple children`() {
        val rootCommand = NoValueRootCommand("ping", "send an ICMP request").apply {
            addCommand(ValueArgumentCommand("-c", "how many to send", Int::class, Regex("\\d")))
            addCommand(ValueArgumentCommand("-d", "d flag help text", Boolean::class, Regex("\\d")))
            addCommand(ValueArgumentCommand("-e", "e flag help text", Boolean::class, Regex("\\d")))
        }
        assertThat(rootCommand.helpText()).isEqualTo("ping: send an ICMP request\n" +
                "-c: how many to send\n" +
                "-d: d flag help text\n" +
                "-e: e flag help text")
    }

    @Test
    fun `validate with no command name returns false`() {
        val rootCommand = NoValueRootCommand("ping", "send an ICMP request")
        assertThat(rootCommand.validate("asdfasdf")).isFalse()
    }

    @Test
    fun `validate with command name returns true`() {
        val rootCommand = NoValueRootCommand("ping", "send an ICMP request")
        assertThat(rootCommand.validate("ping")).isTrue()
    }

    // TODO: not sure how to impl this one
    @Test
    fun `validate with command name and extra junk returns false`() {
        val rootCommand = NoValueRootCommand("ping", "send an ICMP request")
        assertThat(rootCommand.validate("ping asdfa")).isFalse()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parse with invalid input throws exception`() {
        val rootCommand = NoValueRootCommand("ping", "send an ICMP request")
        val input = "banana apple"
        assertThat(rootCommand.validate(input)).isFalse()
        rootCommand.parse(input)
    }

    @Test
    fun `parse with no children returns empty map`() {
        val rootCommand = NoValueRootCommand("ping", "send an ICMP request")
        assertThat(rootCommand.parse("ping")).isEqualTo(mutableMapOf<String, Any>())
    }

    @Test
    fun `parse with no value children returns empty map`() {
        val rootCommand = NoValueRootCommand("ping", "send an ICMP request").apply {
            addCommand(NoValueArgumentCommand("asdf", "doesnt matter whats here"))
            addCommand(NoValueArgumentCommand("vzx", "doesnt matter whats here"))
        }
        assertThat(rootCommand.parse("ping asdf vzx")).isEqualTo(mutableMapOf<String, Any>())
    }
}

class RootCommandTest {

}