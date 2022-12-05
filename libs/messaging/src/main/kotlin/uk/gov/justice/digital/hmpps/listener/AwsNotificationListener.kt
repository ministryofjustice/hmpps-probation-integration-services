package uk.gov.justice.digital.hmpps.listener

import io.awspring.cloud.sqs.annotation.SqsListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.config.AwsCondition
import uk.gov.justice.digital.hmpps.messaging.NotificationHandler

@Component
@Conditional(AwsCondition::class)
@ConditionalOnProperty("messaging.consumer.queue")
class AwsNotificationListener(
    private val handler: NotificationHandler<*>
) {
    @SqsListener("\${messaging.consumer.queue}")
    fun receive(message: String) {
        handler.handle(message)
    }
}
