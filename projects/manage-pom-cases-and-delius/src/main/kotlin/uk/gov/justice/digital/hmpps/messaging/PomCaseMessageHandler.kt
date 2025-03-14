package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.Schema
import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.messaging.Notifier.Companion.BULK_HANDOVER_DATE_UPDATE
import uk.gov.justice.digital.hmpps.services.HandoverDatesChanged
import uk.gov.justice.digital.hmpps.services.PomAllocated
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Channel("manage-pom-cases-and-delius-queue")
class PomCaseMessageHandler(
    @Value("\${mpc.handover.url}") private val mpcHandoverUrl: String,
    override val converter: PomCaseEventConverter,
    private val handoverDatesChanged: HandoverDatesChanged,
    private val pomAllocated: PomAllocated,
    private val telemetryService: TelemetryService,
    private val personRepository: PersonRepository
) : NotificationHandler<Any> {
    @Publish(
        messages = [
            Message(name = "offender-management/handover-changed"),
            Message(name = "offender-management/pom-allocated"),
            Message(title = "SENTENCE_CHANGED", payload = Schema(ProbationOffenderEvent::class)),
            Message(
                title = "pom-handover-dates.internal.bulk-update",
                summary = "Internal use - pom handover date update",
                payload = Schema(HmppsDomainEvent::class)
            )
        ]
    )
    override fun handle(notification: Notification<Any>) {
        telemetryService.notificationReceived(notification)

        try {
            when (val message = notification.message) {
                is HmppsDomainEvent -> when (notification.eventType) {
                    "offender-management.handover.changed", BULK_HANDOVER_DATE_UPDATE ->
                        handoverDatesChanged.process(message.personReference, message.detailUrl, message.dryRun)

                    "offender-management.allocation.changed" -> pomAllocated.process(message)

                    else -> throw NotImplementedError("Unhandled message type received: ${notification.eventType}")
                }

                is ProbationOffenderEvent -> when (notification.eventType) {
                    "SENTENCE_CHANGED" -> personRepository.findNomsSingleCustodial(message.crn)?.let {
                        try {
                            handoverDatesChanged.process(
                                PersonReference(listOf(PersonIdentifier("NOMS", it))),
                                "$mpcHandoverUrl/api/handovers/$it"
                            )
                        } catch (e: HttpClientErrorException.NotFound) {
                            throw IgnorableMessageException(
                                "Handovers api returned not found for sentence changed event",
                                mapOf("detailUrl" to "$mpcHandoverUrl/api/handovers/$it")
                            )
                        }
                    }

                    else -> throw NotImplementedError("Unexpected offender event type: ${notification.eventType}")
                }
            }
        } catch (ime: IgnorableMessageException) {
            telemetryService.trackEvent(ime.message, ime.additionalProperties)
        }
    }

    val HmppsDomainEvent.dryRun get() = additionalInformation["dryRun"] == true
}

@Message
data class ProbationOffenderEvent(val crn: String)

@Primary
@Component
class PomCaseEventConverter(objectMapper: ObjectMapper) : NotificationConverter<Any>(objectMapper) {
    override fun getMessageType() = Any::class

    override fun fromMessage(message: String): Notification<Any> {
        val stringMessage = objectMapper.readValue(message, jacksonTypeRef<Notification<String>>())
        val json = objectMapper.readTree(stringMessage.message)
        if (json.has("crn")) {
            return Notification(
                message = objectMapper.readValue(stringMessage.message, ProbationOffenderEvent::class.java),
                attributes = stringMessage.attributes
            )
        }
        return Notification(
            message = objectMapper.readValue(stringMessage.message, HmppsDomainEvent::class.java),
            attributes = stringMessage.attributes
        )
    }
}
