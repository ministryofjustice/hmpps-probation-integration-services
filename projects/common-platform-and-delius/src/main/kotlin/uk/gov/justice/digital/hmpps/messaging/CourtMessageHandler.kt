package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import io.awspring.cloud.sqs.listener.SqsHeaders
import io.awspring.cloud.sqs.operations.SqsTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Conditional
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.config.AwsCondition
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Conditional(AwsCondition::class)
@Channel("common-platform-and-delius-queue")
class CourtMessageHandler(
    private val converter: NotificationConverter<CommonPlatformHearing>,
    private val telemetryService: TelemetryService,
    private val sqsTemplate: SqsTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${messaging.consumer.court-message-queue}") private val receiveQueue: String,
    @Value("\${messaging.consumer.queue}") private val sendQueue: String
) {
    @Scheduled(fixedDelayString = "\${common-platform.queue.poller.fixed-delay:1000}")
    fun handle() {
        sqsTemplate.receive(receiveQueue, String::class.java).ifPresent { receivedMessage ->

            val notification: Notification<CommonPlatformHearing> = converter.fromMessage(receivedMessage.payload)!!

            telemetryService.trackEvent(
                "CourtMessageHandlerReceived", mapOf(
                    "hearingId" to notification.message.hearing.id
                )
            )

            val hearing = notification.message.hearing
            hearing.prosecutionCases
                .flatMap { case -> case.defendants.map { defendant -> case to defendant } }
                .forEach { (case, defendant) ->
                    val cprUuid = requireNotNull(defendant.cprUUID) { "Missing Core Person UUID" }
                    val newCase = case.copy(defendants = listOf(defendant))
                    val newHearing = hearing.copy(prosecutionCases = listOf(newCase))
                    val newNotification = Notification(
                        message = objectMapper.writeValueAsString(CommonPlatformHearing(newHearing)),
                        attributes = notification.attributes
                    )
                    val outgoingMessage = MessageBuilder.createMessage(
                        objectMapper.writeValueAsString(newNotification),
                        MessageHeaders(mapOf(SqsHeaders.MessageSystemAttributes.SQS_MESSAGE_GROUP_ID_HEADER to cprUuid))
                    )
                    sqsTemplate.send(sendQueue, outgoingMessage)

                    telemetryService.trackEvent(
                        "CourtMessageHandlerSentToFIFO", mapOf(
                            "hearingId" to newHearing.id,
                            "cprUUID" to cprUuid
                        )
                    )
                }
        }
    }
}