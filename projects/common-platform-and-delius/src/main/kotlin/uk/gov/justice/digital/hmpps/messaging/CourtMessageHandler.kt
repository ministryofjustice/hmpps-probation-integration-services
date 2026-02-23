package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.listener.SqsHeaders
import io.awspring.cloud.sqs.operations.SqsTemplate
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.sentry.Sentry
import io.sentry.spring7.tracing.SentryTransaction
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Conditional
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
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
    @Value("\${messaging.consumer.queue}") private val sendQueue: String
) {
    @WithSpan
    @SentryTransaction(operation = "messaging")
    @SqsListener("\${messaging.consumer.court-message-queue}")
    fun handle(receivedMessage: String) {
        try {
            val notification: Notification<CommonPlatformHearing> = converter.fromMessage(receivedMessage) ?: return

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
        } catch (e: Exception) {
            Span.current().recordException(e).setStatus(StatusCode.ERROR)
            Sentry.captureException(e)
            throw e
        }
    }
}