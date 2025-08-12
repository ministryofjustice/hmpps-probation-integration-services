package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientResponseException
import uk.gov.justice.digital.hmpps.client.ManageOffencesClient
import uk.gov.justice.digital.hmpps.client.Offence
import uk.gov.justice.digital.hmpps.config.IgnoredOffence.Companion.IGNORED_OFFENCES
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.OffenceService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Channel("manage-offences-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    private val manageOffencesClient: ManageOffencesClient,
    private val offenceService: OffenceService,
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(messages = [Message(name = "manage-offences/offence-changed")])
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)

        val offence = try {
            manageOffencesClient.getOffence(notification.message.offenceCode)
        } catch (e: RestClientResponseException) {
            if (e.statusCode == HttpStatus.NOT_FOUND) return
            throw e
        }

        IGNORED_OFFENCES.firstOrNull { it.matches(offence) }?.let {
            telemetryService.trackEvent("OffenceCodeIgnored", offence.telemetry + mapOf("reason" to it.reason))
            return
        }

        val isNew = offenceService.createOffence(offence)

        if (isNew) {
            telemetryService.trackEvent("OffenceCodeCreated", offence.telemetry)
        }
    }
}

val HmppsDomainEvent.offenceCode get() = additionalInformation["offenceCode"] as String

val Offence.telemetry
    get() = listOfNotNull(
        "offenceCode" to code,
        homeOfficeCode?.let { "homeOfficeCode" to it }
    ).toMap()