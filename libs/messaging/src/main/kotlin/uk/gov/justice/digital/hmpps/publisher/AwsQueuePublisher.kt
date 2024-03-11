package uk.gov.justice.digital.hmpps.publisher

import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.operations.SqsTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Conditional
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.config.AwsCondition
import uk.gov.justice.digital.hmpps.message.Notification

@Component
@Conditional(AwsCondition::class)
@ConditionalOnProperty("messaging.producer.queue")
class AwsQueuePublisher(
    private val sqsTemplate: SqsTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${messaging.producer.queue}") private val queue: String
) : NotificationPublisher {
    override fun publish(notification: Notification<*>) {
        notification.message?.let { message ->
            sqsTemplate.sendAsync(
                queue, MessageBuilder.createMessage(
                    objectMapper.writeValueAsString(
                        Notification(
                            message = objectMapper.writeValueAsString(message),
                            notification.attributes
                        )
                    ),
                    MessageHeaders(notification.attributes.map { it.key to it.value.value }.toMap())
                )
            )
        }
    }
}
