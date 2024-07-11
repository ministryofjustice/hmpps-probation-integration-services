package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.Notifier.Companion.BULK_HANDOVER_DATE_UPDATE
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

    @BeforeEach
    fun setUp() {
        handler = PomCaseMessageHandler(
            "localhost",
            converter,
            handoverDatesChanged,
            pomAllocated,
            telemetryService,
            personRepository
        )
    }

    @Test
    fun `handles unexpected event type`() {
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

    @Test
    fun `handles internal event type`() {
        doNothing().whenever(pomAllocated).process(any<HmppsDomainEvent>())
        handler.handle(
            Notification(
                message = HmppsDomainEvent(
                    eventType = BULK_HANDOVER_DATE_UPDATE,
                    version = 1
                ),
                attributes = MessageAttributes(eventType = BULK_HANDOVER_DATE_UPDATE)
            )
        )

        verify(pomAllocated, times(1)).process(any<HmppsDomainEvent>())
        verifyNoMoreInteractions(pomAllocated)
    }
}
