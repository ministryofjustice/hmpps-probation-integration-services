package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.service.entity.EventRepository
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.text.get
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class GoalsIntegrationTest @Autowired constructor(
    @Value("\${messaging.consumer.queue}") private val queueName: String,
    private val channelManager: HmppsChannelManager,
    private val eventRepository: EventRepository,
    @MockitoBean private val telemetryService: TelemetryService,
) {

    @Test
    fun `goals completed sets sp_goals_complete and sp_goals_date on active events`() {
        val crn = PersonGenerator.DEFAULT.crn
        val occurredAt = ZonedDateTime.parse("2026-01-02T03:04:05Z")
        val message = HmppsDomainEvent(
            eventType = "arns.sentence.plan.goals.completed",
            version = 1,
            occurredAt = occurredAt,
            description = "No more open goals",
            personReference = PersonReference(listOf(PersonIdentifier("CRN", crn))),
            additionalInformation = mapOf("planUuid" to "some-uuid")
        )
        val notification =
            Notification(message = message, attributes = MessageAttributes(eventType = message.eventType))

        channelManager.getChannel(queueName).publishAndWait(notification)

        val event = eventRepository.findById(EventGenerator.DEFAULT_EVENT.id).get()
        assertThat(event.spGoalsComplete?.trim()).isEqualTo("Y")
        assertThat(event.spGoalsDate).isEqualTo(occurredAt.toLocalDate())
    }

    @Test
    fun `goals added clears sp_goals_complete and sp_goals_date on active events`() {
        val crn = PersonGenerator.DEFAULT.crn
        // First set the fields
        val event = eventRepository.findById(EventGenerator.DEFAULT_EVENT.id).get()
        event.spGoalsComplete = "Y"
        event.spGoalsDate = LocalDate.now()
        eventRepository.save(event)

        val message = HmppsDomainEvent(
            eventType = "arns.sentence.plan.goals.added",
            version = 1,
            occurredAt = ZonedDateTime.now(),
            description = "There is an open goal",
            personReference = PersonReference(listOf(PersonIdentifier("CRN", crn))),
            additionalInformation = mapOf("planUuid" to "some-uuid")
        )
        val notification =
            Notification(message = message, attributes = MessageAttributes(eventType = message.eventType))

        channelManager.getChannel(queueName).publishAndWait(notification)

        val updated = eventRepository.findById(EventGenerator.DEFAULT_EVENT.id).get()
        assertThat(updated.spGoalsComplete).isNull()
        assertThat(updated.spGoalsDate).isNull()
    }

    @Test
    fun `goals completed logs telemetry when no active disposals found`() {
        val crn = PersonGenerator.NON_CUSTODIAL.crn  // or use a CRN with no active disposals
        val message = HmppsDomainEvent(
            eventType = "arns.sentence.plan.goals.completed",
            version = 1,
            occurredAt = ZonedDateTime.now(),
            description = "No more open goals",
            personReference = PersonReference(listOf(PersonIdentifier("CRN", "UNKNOWN1"))),
            additionalInformation = mapOf("planUuid" to "some-uuid")
        )
        val notification =
            Notification(message = message, attributes = MessageAttributes(eventType = message.eventType))

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).trackEvent(
            eq("GoalsAchievedNoActiveDisposal"),
            argThat<Map<String, String>> { this["crn"] == "UNKNOWN1" },
            any()
        )
    }

    @Test
    fun `goals added logs telemetry when no active disposals found`() {
        val message = HmppsDomainEvent(
            eventType = "arns.sentence.plan.goals.added",
            version = 1,
            occurredAt = ZonedDateTime.now(),
            description = "There is an open goal",
            personReference = PersonReference(listOf(PersonIdentifier("CRN", "UNKNOWN1"))),
            additionalInformation = mapOf("planUuid" to "some-uuid")
        )
        val notification =
            Notification(message = message, attributes = MessageAttributes(eventType = message.eventType))

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).trackEvent(
            eq("GoalsAchievedRemovedNoActiveDisposal"),
            argThat<Map<String, String>> { this["crn"] == "UNKNOWN1" },
            any()
        )
    }
}