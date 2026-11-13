package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import io.awspring.cloud.sqs.listener.SqsHeaders
import io.awspring.cloud.sqs.operations.SqsTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

/**
 * This handler receives a notification from the SQS queue, splits it into multiple messages (one per defendant), and
 * sends them individually to the FIFO queue with a message group ID of the defendant's CPR UUID. This ensures that
 * messages for the same defendant cannot be processed in parallel.
 */
@Component
@Channel("common-platform-and-delius-queue")
class MessageGroupingHandler(
    override val converter: NotificationConverter<CommonPlatformHearing>,
    private val telemetryService: TelemetryService,
    private val sqsTemplate: SqsTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${messaging.consumer.queue}") private val sendQueue: String
) : NotificationHandler<CommonPlatformHearing> {
    override fun handle(notification: Notification<CommonPlatformHearing>) {
        telemetryService.trackEvent(
            "CourtMessageHandlerReceived", mapOf(
                "hearingId" to notification.message.hearing.id
            )
        )

        val hearing = notification.message.hearing
        hearing.prosecutionCases
            .flatMap { case -> case.defendants.map { defendant -> case to defendant } }
            .forEach { (case, defendant) ->
                if (defendant.personDefendant == null || defendant.cprUUID == null) return@forEach
                val newCase = case.copy(defendants = listOf(defendant))
                val newHearing = hearing.copy(prosecutionCases = listOf(newCase))
                val newNotification = Notification(
                    message = objectMapper.writeValueAsString(CommonPlatformHearing(newHearing)),
                    attributes = notification.attributes
                )
                val outgoingMessage = MessageBuilder.createMessage(
                    objectMapper.writeValueAsString(newNotification),
                    MessageHeaders(mapOf(SqsHeaders.MessageSystemAttributes.SQS_MESSAGE_GROUP_ID_HEADER to defendant.cprUUID))
                )
                sqsTemplate.send(sendQueue, outgoingMessage)

                telemetryService.trackEvent(
                    "CourtMessageHandlerSentToFIFO", mapOf(
                        "hearingId" to newHearing.id,
                        "cprUUID" to defendant.cprUUID
                    )
                )
            }
    }
}