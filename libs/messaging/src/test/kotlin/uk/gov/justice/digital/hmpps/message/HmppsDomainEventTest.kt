package uk.gov.justice.digital.hmpps.message

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class HmppsDomainEventTest {
    @Test
    fun `can find crn successfully`() {
        val hmppsEvent =
            HmppsDomainEvent(
                "test.event.type",
                1,
                "https//detail/url",
                ZonedDateTime.now(),
                personReference =
                    PersonReference(
                        listOf(PersonIdentifier("CRN", "X123456")),
                    ),
            )

        assertThat(hmppsEvent.personReference.findCrn(), equalTo("X123456"))
    }

    @Test
    fun `can find identifier successfully`() {
        val hmppsEvent =
            HmppsDomainEvent(
                "test.event.type",
                1,
                "https//detail/url",
                ZonedDateTime.now(),
                personReference =
                    PersonReference(
                        listOf(PersonIdentifier("NOMS_NUMBER", "A123456BC")),
                    ),
            )

        assertThat(hmppsEvent.personReference["NOMS_NUMBER"], equalTo("A123456BC"))
    }

    @Test
    fun `does not error if identifier does not exist`() {
        val hmppsEvent =
            HmppsDomainEvent(
                "test.event.type",
                1,
                "https//detail/url",
                ZonedDateTime.now(),
            )

        assertNull(hmppsEvent.personReference["UNKNOWN_ID"])
        assertNull(hmppsEvent.personReference.findCrn())
    }

    @Test
    fun `can set and retrieve additional information`() {
        val hmppsEvent =
            HmppsDomainEvent(
                "test.event.type",
                1,
                "https//detail/url",
                ZonedDateTime.now(),
            )

        hmppsEvent.additionalInformation["specialId"] = "SP12345"

        assertThat(hmppsEvent.additionalInformation["specialId"], equalTo("SP12345"))
    }
}
