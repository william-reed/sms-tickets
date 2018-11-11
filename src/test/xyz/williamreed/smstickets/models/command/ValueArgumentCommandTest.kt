package xyz.williamreed.smstickets.models.command

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.lang.IllegalArgumentException

class ValueArgumentCommandTest {

    private val mandatoryCommand = ValueArgumentCommand("-c", "Amount of pings to send", Int::class, Regex("\\d+"), false)
    private val optionalCommand = ValueArgumentCommand("-c", "Amount of pings to send", Int::class, Regex("\\d+"), true)

    @Test
    fun `command help text is right`() {
        assertThat(mandatoryCommand.helpText).isEqualTo("Amount of pings to send")
    }

    @Test
    fun `command name is right`() {
        assertThat(mandatoryCommand.name).isEqualTo("-c")
    }

    @Test
    fun `command is mandatory`() {
        assertThat(mandatoryCommand.optional).isFalse()
    }

    @Test
    fun `optional command is optional`() {
        assertThat(optionalCommand.optional).isTrue()
    }

    @Test
    fun `mandatory command does not validates with empty input`() {
        assertThat(mandatoryCommand.validate("")).isFalse()
    }

    @Test
    fun `optional command does validate with empty input`() {
        assertThat(optionalCommand.validate("")).isTrue()
    }

    @Test
    fun `mandatory command does not validate with non relevant input`() {
        assertThat(mandatoryCommand.validate("ping -n 3")).isFalse()
    }

    @Test
    fun `optional command validates with non relevant input`() {
        assertThat(optionalCommand.validate("ping -n 3")).isTrue()
    }

    @Test
    fun `mandatory command does not validate with only command name in input`() {
        assertThat(mandatoryCommand.validate("-c")).isFalse()
    }

    @Test
    fun `mandatory command does not validate with command name and improper value`() {
        assertThat(mandatoryCommand.validate("-c abc")).isFalse()
    }

    @Test
    fun `mandatory command does not validate with command name and proper value within string`() {
        assertThat(mandatoryCommand.validate("-c ab14c")).isFalse()
    }

    @Test
    fun `mandatory command does not validate with command name and proper value within string 2`() {
        assertThat(mandatoryCommand.validate("-c 14c")).isFalse()
    }

    @Test
    fun `mandatory command does not validate with command name and proper value within string 3`() {
        assertThat(mandatoryCommand.validate("-c c14")).isFalse()
    }

    @Test
    fun `mandatory command does validates with substring input`() {
        assertThat(mandatoryCommand.validate("-c 14")).isTrue()
    }

    @Test
    fun `mandatory command does not validate what no value is given`() {
        assertThat(mandatoryCommand.validate("ping -c -A 192.168.1.1")).isFalse()
    }

    @Test
    fun `mandatory command does not validate with no value given with substring input`() {
        assertThat(mandatoryCommand.validate("-c")).isFalse()
    }

    @Test
    fun `mandatory command does validate with full input`() {
        assertThat(mandatoryCommand.validate("ping -c 3 -A 192.168.1.1")).isTrue()
    }

    @Test
    fun `optional command does not with empty input`() {
        assertThat(optionalCommand.validate("")).isTrue()
    }

    @Test
    fun `optional command does validate with non relevant input`() {
        assertThat(optionalCommand.validate("ping -A 192.168.1.1")).isTrue()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parse with invalid input throws exception`() {
        mandatoryCommand.parse("ping -A 192.168.1.1", mutableMapOf())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parse with invalid gibberish input throws exception`() {
        mandatoryCommand.parse("asdf asd oasdfo asfd", mutableMapOf())
    }

    @Test
    fun `parse with invalid input on optional command returns input map`() {
        val map = mutableMapOf<String, String>().apply {
            put("garbage", "data")
        }
        assertThat(optionalCommand.parse("ping -A 192.168.1.1", map)).isEqualTo(map)
    }

    @Test
    fun `parse with valid substring input gives proper values back`() {
        val expectedMap = mapOf("-c" to "3")
        assertThat(mandatoryCommand.parse("-c 3", mutableMapOf())).isEqualTo(expectedMap)
    }

    @Test
    fun `parse with valid input gives proper values back`() {
        val expectedMap = mapOf("-c" to "3")
        assertThat(mandatoryCommand.parse("ping -c 3 -A 192.168.1.1", mutableMapOf()))
                .isEqualTo(expectedMap)
    }

    @Test
    fun `strip with empty string returns empty string`() {
        assertThat(mandatoryCommand.strip("")).isEqualTo("")
    }

    @Test
    fun `strip with random input gives back random input`() {
        val randomInput = "asdfa sdf asdf asdf asdf asd f"
        assertThat(mandatoryCommand.strip(randomInput)).isEqualTo(randomInput)
    }

    // these next commands demonstrate what happens to white space
    @Test
    fun `strip with proper input in middle gives other input back`() {
        assertThat(mandatoryCommand.strip("ping -c 3 -A 192.168.1.1")).isEqualTo("ping   -A 192.168.1.1")
    }

    @Test
    fun `strip with proper input at the start gives other input back`() {
        assertThat(mandatoryCommand.strip("-c 3 192.168.1.1")).isEqualTo("  192.168.1.1")
    }

    @Test
    fun `strip with proper input at the end gives other input back`() {
        assertThat(mandatoryCommand.strip("192.168.1.1 -c 3")).isEqualTo("192.168.1.1  ")
    }

    @Test
    fun `strip with whitespace as tab before argument`() {
        assertThat(mandatoryCommand.strip("ping     -c 3 -A 192.168.1.1")).isEqualTo("ping       -A 192.168.1.1")
    }

    @Test
    fun `strip with whitespace as tab after argument`() {
        assertThat(mandatoryCommand.strip("ping -c 3    -A 192.168.1.1")).isEqualTo("ping      -A 192.168.1.1")
    }

    @Test
    fun `strip with whitespace as tab before and after argument`() {
        assertThat(mandatoryCommand.strip("ping     -c 3    -A 192.168.1.1")).isEqualTo("ping          -A 192.168.1.1")
    }

    @Test
    fun `strip with just command name returns everything after it`() {
        assertThat(mandatoryCommand.strip("ping -c random input")).isEqualTo("ping  random input")
    }

    @Test
    fun `strip with just values returns everything`() {
        assertThat(mandatoryCommand.strip("ping random input")).isEqualTo("ping random input")
    }
}