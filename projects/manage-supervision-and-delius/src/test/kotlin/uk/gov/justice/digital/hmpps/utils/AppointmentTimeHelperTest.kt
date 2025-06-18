package uk.gov.justice.digital.hmpps.utils


import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import java.time.ZonedDateTime

class AppointmentTimeHelperTest {

    @ParameterizedTest
    @MethodSource("times")
    fun `start and end time is formated correctly`(start: ZonedDateTime, end: ZonedDateTime, expected: String) {
        assertThat(AppointmentTimeHelper.startAndEnd(start, end)).isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        fun times() = listOf(
            Arguments.of(
                ZonedDateTime.of(2024, 11, 27, 10, 15, 0, 0, EuropeLondon),
                ZonedDateTime.of(2024, 11, 27, 11, 15, 0, 0, EuropeLondon),
                "10:15am to 11:15am"),
            Arguments.of(
                ZonedDateTime.of(2025, 7, 10, 12, 15, 0, 0, EuropeLondon),
                ZonedDateTime.of(2025, 7, 10, 13, 15, 0, 0, EuropeLondon),
                "12:15pm to 1:15pm"),
            Arguments.of(
                ZonedDateTime.of(2025, 7, 10, 18, 0, 0, 0, EuropeLondon),
                ZonedDateTime.of(2025, 7, 10, 21, 15, 0, 0, EuropeLondon),
                "6pm to 9:15pm"),
        )
    }
}