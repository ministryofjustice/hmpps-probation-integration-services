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
            bookingUrl = "B",
            departedAt = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, EuropeLondon),
            notes = "C",
            reason = "D",
            reasonDetail = null,
            moveOnCategory = Category(description = "E")
        )
        assertThat(event.noteText, equalTo("Departure date: 01/01/2020${System.lineSeparator()}C${System.lineSeparator()}D${System.lineSeparator()}E"))
    }
}
