package uk.gov.justice.digital.hmpps.integrations.prison

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class MovementTest {
    @ParameterizedTest
    @MethodSource("movementTypes")
    fun `correct type is returned`(
        booking: Booking,
        expected: String?,
    ) {
        assertThat(booking.reason, equalTo(expected))
    }

    companion object {
        val BOOKING = Booking(253615, "45678A", true, "A1234BC", "SWI", "ADM", "", Booking.InOutStatus.IN)

        @JvmStatic
        fun movementTypes() =
            listOf(
                Arguments.of(BOOKING.copy(movementReason = "N"), "ADMISSION"),
                Arguments.of(BOOKING.copy(movementReason = "INT"), "TRANSFERRED"),
                Arguments.of(BOOKING.copy(movementReason = "TRNCRT"), "TRANSFERRED"),
                Arguments.of(BOOKING.copy(movementReason = "TRNTAP"), "TRANSFERRED"),
                Arguments.of(BOOKING.copy(movementType = "TAP"), "TEMPORARY_ABSENCE_RETURN"),
                Arguments.of(
                    BOOKING.copy(movementType = "TAP", inOutStatus = Booking.InOutStatus.OUT),
                    "TEMPORARY_ABSENCE_RELEASE",
                ),
                Arguments.of(BOOKING.copy(movementType = "CRT"), "RETURN_FROM_COURT"),
                Arguments.of(BOOKING.copy(movementType = "CRT", inOutStatus = Booking.InOutStatus.OUT), "SENT_TO_COURT"),
                Arguments.of(BOOKING.copy(movementType = "REL", inOutStatus = Booking.InOutStatus.OUT), "RELEASED"),
                Arguments.of(
                    BOOKING.copy(movementType = "REL", movementReason = "HO", inOutStatus = Booking.InOutStatus.OUT),
                    "RELEASED_TO_HOSPITAL",
                ),
                Arguments.of(
                    BOOKING.copy(movementType = "REL", movementReason = "HP", inOutStatus = Booking.InOutStatus.OUT),
                    "RELEASED_TO_HOSPITAL",
                ),
                Arguments.of(
                    BOOKING.copy(movementType = "REL", movementReason = "HQ", inOutStatus = Booking.InOutStatus.OUT),
                    "RELEASED_TO_HOSPITAL",
                ),
                Arguments.of(BOOKING.copy(movementType = "OTHER", movementReason = "ANY"), null),
            )
    }
}
