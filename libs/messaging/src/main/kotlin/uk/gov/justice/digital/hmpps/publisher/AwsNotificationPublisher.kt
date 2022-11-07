package uk.gov.justice.digital.hmpps.publisher

import io.awspring.cloud.messaging.core.NotificationMessagingTemplate
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.config.aws.AwsConfig
import uk.gov.justice.digital.hmpps.message.Notification

@Component
@ConditionalOnBean(AwsConfig::class)
class AwsNotificationPublisher(
    private val notificationTemplate: NotificationMessagingTemplate
) : NotificationPublisher {
    override fun publish(notification: Notification<*>) {
        notification.message?.let { message ->
            notificationTemplate.convertAndSend(message) { msg ->
                MessageBuilder.createMessage(
                    msg.payload,
                    MessageHeaders(notification.attributes.map { it.key to it.value.value }.toMap())
                )
            }
        }
    }
}
