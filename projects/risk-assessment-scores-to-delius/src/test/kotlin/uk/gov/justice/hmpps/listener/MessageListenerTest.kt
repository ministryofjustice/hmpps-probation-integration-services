package uk.gov.justice.hmpps.listener

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.MessageGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.RiskScoreService
import uk.gov.justice.digital.hmpps.listener.MessageListener
import uk.gov.justice.digital.hmpps.listener.assessmentDate
import uk.gov.justice.digital.hmpps.listener.ospContact
import uk.gov.justice.digital.hmpps.listener.ospIndecent
import uk.gov.justice.digital.hmpps.listener.rsr
import uk.gov.justice.digital.hmpps.listener.telemetryProperties
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
internal class MessageListenerTest {
    @Mock lateinit var telemetryService: TelemetryService
    @Mock lateinit var riskScoreService: RiskScoreService
    @InjectMocks lateinit var messageListener: MessageListener

    @Test
    fun `message is logged to telemetry`() {
        // Given a message
        val message = Notification(
            message = MessageGenerator.RSR_SCORES_DETERMINED,
            attributes = MessageAttributes("risk-assessment.scores.rsr.determined")
        )

        // When it is received
        messageListener.receive(message)

        // Then it is logged to telemetry
        verify(telemetryService).notificationReceived(message)
    }

    @Test
    fun `RSR messages are processed`() {
        // Given an RSR message
        val message = Notification(
            message = MessageGenerator.RSR_SCORES_DETERMINED,
            attributes = MessageAttributes("risk-assessment.scores.rsr.determined")
        )

        // When it is received
        messageListener.receive(message)

        // Then it is processed
        verify(riskScoreService).updateRsrScores(
            message.message.personReference.findCrn()!!,
            message.message.additionalInformation["EventNumber"] as Int,
            message.message.assessmentDate(),
            message.message.rsr(),
            message.message.ospIndecent(),
            message.message.ospContact()
        )
        verify(telemetryService).trackEvent("RsrScoresUpdated", message.message.telemetryProperties())
    }

    @Test
    fun `OGRS messages are ignored`() {
        // Given an OGRS message
        val message = Notification(
            message = MessageGenerator.OGRS_SCORES_DETERMINED,
            attributes = MessageAttributes("risk-assessment.scores.ogrs.determined")
        )

        // When it is received
        messageListener.receive(message)

        // Then it is not processed
        verifyNoInteractions(riskScoreService)
        verify(telemetryService).trackEvent("UnsupportedEventType", message.message.telemetryProperties())
    }

    @Test
    fun `unknown messages are thrown`() {
        assertThrows<IllegalArgumentException> {
            messageListener.receive(
                Notification(
                    message = MessageGenerator.RSR_SCORES_DETERMINED,
                    attributes = MessageAttributes("unknown")
                )
            )
        }
    }

    @Test
    fun `missing CRN is thrown`() {
        assertThrows<IllegalArgumentException> {
            messageListener.receive(
                Notification(
                    message = MessageGenerator.RSR_SCORES_DETERMINED.copy(personReference = PersonReference()),
                    attributes = MessageAttributes("risk-assessment.scores.rsr.determined")
                )
            )
        }
    }
}
