package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.service.PncNumber

internal class PncNumberTest {
    @ParameterizedTest
    @MethodSource("pncNumbers")
    fun `can create from string`(value: String, matchValue: String) {
        assertThat(PncNumber.from(value)?.matchValue()).isEqualTo(matchValue)
    }

    companion object {
        @JvmStatic
        fun pncNumbers(): List<Arguments> = listOf(
            Arguments.of("2024/0562552J", "2024/0562552J"),
            Arguments.of("97/543794F", "1997/0543794F"),
            Arguments.of("09/516048H", "2009/0516048H"),
            Arguments.of("25/0214916J", "2025/0214916J"),
            Arguments.of("1999/0472086T", "1999/0472086T")
        )
    }
}