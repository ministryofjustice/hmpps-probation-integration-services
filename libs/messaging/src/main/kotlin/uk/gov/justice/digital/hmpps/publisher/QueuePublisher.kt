package uk.gov.justice.digital.hmpps.publisher

import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.operations.SqsTemplate
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Conditional
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.config.AwsCondition
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.withSpanContext
import java.util.concurrent.Semaphore

@Component
@Conditional(AwsCondition::class)
@ConditionalOnProperty("messaging.producer.queue")
class QueuePublisher(
    private val sqsTemplate: SqsTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${messaging.producer.queue}") private val queue: String,
    @Value("\${messaging.producer.concurrency:1000}") private val limit: Int
) : NotificationPublisher {

    private val permit = Semaphore(limit, true)

    @WithSpan(kind = SpanKind.PRODUCER)
    override fun publish(notification: Notification<*>) {
        notification.message?.also { _ ->
            permit.acquire()
            try {
                sqsTemplate.send(queue, notification.asMessage())
            } finally {
                permit.release()
            }
        }
    }

    private fun Notification<*>.asMessage() = MessageBuilder.createMessage(
        objectMapper.writeValueAsString(Notification(objectMapper.writeValueAsString(message), attributes)),
        MessageHeaders(attributes.map { it.key to it.value.value }.toMap()).withSpanContext()
    )
}
