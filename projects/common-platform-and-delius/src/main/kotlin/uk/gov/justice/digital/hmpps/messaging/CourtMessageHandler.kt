package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.listener.SqsHeaders
import io.awspring.cloud.sqs.operations.SqsTemplate
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Conditional
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.config.AwsCondition
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDateTime

@Component
@Conditional(AwsCondition::class)
@Channel("common-platform-and-delius-queue")
class CourtMessageHandler(
    private val converter: NotificationConverter<CommonPlatformHearing>,
    private val telemetryService: TelemetryService,
    private val sqsTemplate: SqsTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${messaging.consumer.court-message-queue}") private val receiveQueue: String,
    @Value("\${messaging.consumer.queue}") private val sendQueue: String,
    @Value("\${common-platform.queue.working-hours:7-18}") private val workingHours: String,
    @Value("\${common-platform.queue.working-days:1-5}") private val workingDays: String
) {

    private var lastReceivedMessageTime: LocalDateTime? = null

    @Scheduled(fixedDelayString = "\${common-platform.queue.poller.fixed-delay:1000}")
    fun handle() {
        sqsTemplate.receive(receiveQueue, String::class.java).ifPresent { receivedMessage ->
            lastReceivedMessageTime = LocalDateTime.now()

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
                    val cprUuid = defendant.cprUUID
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

    @Scheduled(fixedDelayString = "\${common-platform.queue.message-inactivity-time:1800000}")
    fun checkMessageInactivity() {
        val now = LocalDateTime.now()

        val (startHour, endHour) = workingHours.split("-").map { it.toInt() }
        val (startDay, endDay) = workingDays.split("-").map { it.toInt() }
        val inBusinessHours = now.hour in startHour..endHour && now.dayOfWeek.value in startDay..endDay

        if (inBusinessHours && (lastReceivedMessageTime == null || lastReceivedMessageTime!!.isBefore(now.minusHours(1)))) {
            val event = SentryEvent()
            val message = Message()
            message.message = "No common platform messages received in the last hour"
            event.message = message
            event.level = SentryLevel.WARNING
            Sentry.captureEvent(event)
        }
    }
}