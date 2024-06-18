package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.services.HandoverDatesChanged
import uk.gov.justice.digital.hmpps.services.PomAllocated
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {

    @Mock
    lateinit var converter: PomCaseEventConverter

    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var handoverDatesChanged: HandoverDatesChanged

    @Mock
    lateinit var pomAllocated: PomAllocated

    lateinit var handler: PomCaseMessageHandler

    @Test
    fun `handles unexpected event type`() {
        handler = PomCaseMessageHandler(
            "localhost",
            converter,
            handoverDatesChanged,
            pomAllocated,
            telemetryService,
            personRepository
        )
        val exception = assertThrows<NotImplementedError> {
            handler.handle(
                Notification(
                    message = MessageGenerator.SENTENCE_CHANGED,
                    attributes = MessageAttributes(eventType = "UNKNOWN")
                )
            )
        }
        assertThat(exception.message, equalTo("Unexpected offender event type: UNKNOWN"))
    }
}
