package uk.gov.justice.digital.hmpps.publisher

import io.awspring.cloud.sns.core.SnsTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Primary
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.config.AwsCondition
import uk.gov.justice.digital.hmpps.message.Notification

@Primary
@Component
@Conditional(AwsCondition::class)
@ConditionalOnProperty("messaging.producer.topic")
class AwsNotificationPublisher(
    private val notificationTemplate: SnsTemplate,
    @Value("\${messaging.producer.topic}") private val topic: String,
) : NotificationPublisher {
    override fun publish(notification: Notification<*>) {
        notification.message?.let { message ->
            notificationTemplate.convertAndSend(topic, message) { msg ->
                MessageBuilder.createMessage(
                    msg.payload,
                    MessageHeaders(notification.attributes.map { it.key to it.value.value }.toMap()),
                )
            }
        }
    }
}
