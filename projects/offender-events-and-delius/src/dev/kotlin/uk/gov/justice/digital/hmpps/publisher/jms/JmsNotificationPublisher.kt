package uk.gov.justice.digital.hmpps.publisher.jms

import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.config.AwsConfig
import uk.gov.justice.digital.hmpps.integrations.delius.NotificationPublisher
import uk.gov.justice.digital.hmpps.message.Notification

@Component
@ConditionalOnClass(ActiveMQConnectionFactory::class)
@ConditionalOnMissingBean(AwsConfig::class)
class JmsNotificationPublisher(private val jmsTemplate: JmsTemplate) : NotificationPublisher {
    override fun publish(notification: Notification<*>) {
        jmsTemplate.convertAndSend(notification)
    }
}
