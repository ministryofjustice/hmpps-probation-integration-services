package uk.gov.justice.digital.hmpps.listener

import io.awspring.cloud.messaging.listener.annotation.SqsListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.messaging.NotificationHandler

@Service
@ConditionalOnProperty("messaging.consumer.queue")
@ConditionalOnMissingClass("org.apache.activemq.ActiveMQConnectionFactory")
class AwsNotificationListener(
    converter: NotificationConverter<*>,
    handler: NotificationHandler<*>
) : NotificationListener(converter, handler) {

    @SqsListener("\${messaging.consumer.queue}")
    fun receive(message: String) {
        convertAndHandle(message)
    }
}
