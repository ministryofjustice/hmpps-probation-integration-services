package uk.gov.justice.digital.hmpps.integrations.approvedpremesis

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import java.time.ZonedDateTime

class EventDetailsTest {
    @Test
    fun `person departed notes handles null reasonDetail`() {
        val event = PersonDeparted(
            applicationId = null,
            applicationUrl = null,
            bookingId = "A",
            bookingUrl = "BookingURL",
            departedAt = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, EuropeLondon),
            notes = "Notes",
            reason = "Reason",
            reasonDetail = "ReasonDetail",
            moveOnCategory = Category(description = "MoveOnCat"),
            recordedBy = By("N03HPT1", "N03")
        )
        assertThat(
            event.noteText,
            equalTo("Departure date: 01/01/2020${System.lineSeparator()}Reason${System.lineSeparator()}ReasonDetail${System.lineSeparator()}MoveOnCat${System.lineSeparator()}Notes")
        )
    }
}
