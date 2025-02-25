package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.Schema
import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.config.security.nullIfNotFound
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException.Companion.orIgnore
import uk.gov.justice.digital.hmpps.integrations.oasys.OrdsClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.AssessmentSubmitted
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI

const val AssessmentSummaryProduced = "assessment.summary.produced"

@Component
@Channel("assessment-summary-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val ordsClient: OrdsClient,
    private val assessmentSubmitted: AssessmentSubmitted,
    private val telemetryService: TelemetryService
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(messages = [Message(title = AssessmentSummaryProduced, payload = Schema(HmppsDomainEvent::class))])
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        try {
            if (notification.message.eventType == AssessmentSummaryProduced) {
                telemetryService.notificationReceived(notification)
                notification.message.detailUrl
                    ?.let { nullIfNotFound { ordsClient.getAssessmentSummary(URI.create(it)) }.orIgnore { "No assessment in OASys" } }
                    ?.let { assessmentSubmitted.assessmentSubmitted(it.crn, it.assessments.first()) }
            }
        } catch (ime: IgnorableMessageException) {
            telemetryService.trackEvent(
                "AssessmentSummaryFailureReport",
                notification.message.telemetryProperties() + ime.additionalProperties + ("reason" to ime.message)
            )
        }
    }
}

fun HmppsDomainEvent.telemetryProperties() = listOfNotNull(
    "occurredAt" to occurredAt.toString(),
    "crn" to crn(),
    detailUrl?.let { "detailUrl" to it }
).toMap()

fun HmppsDomainEvent.crn(): String = personReference.findCrn() ?: throw IllegalArgumentException("Missing CRN")
