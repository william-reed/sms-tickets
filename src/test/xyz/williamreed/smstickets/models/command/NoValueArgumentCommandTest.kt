package xyz.williamreed.smstickets.models.command

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NoValueArgumentCommandTest {

    private val command = NoValueArgumentCommand("-A", "Output a sound when a packet is not received before another is sent")

    @Test
    fun `command help text is right`() {
        assertThat(command.helpText).isEqualTo("Output a sound when a packet is not received before another is sent")
    }

    @Test
    fun `command name is right`() {
        assertThat(command.name).isEqualTo("-A")
    }

    @Test
    fun `NoValueArgumentCommand is always optional`() {
        assertThat(command.optional).isTrue()
    }

    @Test
    fun `command validates with empty input`() {
        assertThat(command.validate("")).isTrue()
    }

    @Test
    fun `command validates with non relevant input`() {
        assertThat(command.validate("ping -n 3")).isTrue()
    }

    @Test
    fun `command validates with proper input`() {
        assertThat(command.validate("-A")).isTrue()
    }

    // There is no reason to ever have mandatory argument commands. They would just represent default behavior
//    @Test
//    fun `non optional command does not validate with empty input`() {
//        assertThat(mandatoryCommand.validate("")).isFalse()
//    }
//
//    @Test
//    fun `non optional command does not validate with non relevant input`() {
//        assertThat(mandatoryCommand.validate("ping -n 3")).isFalse()
//    }

    @Test
    fun `parse just returns given map`() {
        val valuesMap = mutableMapOf<String, String>().apply {
            put("ping", "192.168.1.1")
            put("-c", "3")
        }
        assertThat(command.parse("ping -c 3 -A 192.168.1.1", valuesMap)).isEqualTo(valuesMap)
    }

    @Test
    fun `strip with empty string returns empty string`(){
        assertThat(command.strip("")).isEqualTo("")
    }

    @Test
    fun `strip with random input gives random input back`() {
        val nonsense = "asdfa asdf iasd asbfabsdib sf"
        assertThat(command.strip(nonsense)).isEqualTo(nonsense)
    }

    // these next commands demonstrate what happens to white space
    @Test
    fun `strip with proper input in middle gives other input back`() {
        assertThat(command.strip("ping -c 3 -A 192.168.1.1")).isEqualTo("ping -c 3  192.168.1.1")
    }

    @Test
    fun `strip with proper input at the start gives other input back`() {
        assertThat(command.strip("-A 192.168.1.1")).isEqualTo(" 192.168.1.1")
    }

    @Test
    fun `strip with proper input at the end gives other input back`() {
        assertThat(command.strip("192.168.1.1 -A")).isEqualTo("192.168.1.1 ")
    }

    @Test
    fun `strip with whitespace as tab before argument`() {
        assertThat(command.strip("ping -c 3     -A 192.168.1.1")).isEqualTo("ping -c 3      192.168.1.1")
    }

    @Test
    fun `strip with whitespace as tab after argument`() {
        assertThat(command.strip("ping -c 3 -A    192.168.1.1")).isEqualTo("ping -c 3     192.168.1.1")
    }

    @Test
    fun `strip with whitespace as tab before and after argument`() {
        assertThat(command.strip("ping -c 3     -A    192.168.1.1")).isEqualTo("ping -c 3         192.168.1.1")
    }
}