package uk.gov.justice.digital.hmpps.messaging

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Conditional
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.config.AwsCondition
import uk.gov.justice.digital.hmpps.listener.AwsNotificationListener

@Component
@Conditional(AwsCondition::class)
@ConditionalOnExpression("\${messaging.consumer.enabled:true} and '\${messaging.consumer.court-message-queue:}' != ''")
class MessageGroupingListener(
    handler: MessageGroupingHandler,
    objectMapper: ObjectMapper,
    taskScheduler: SimpleAsyncTaskScheduler,
    @Value("\${messaging.consumer.court-message-queue}") private val queueName: String,
    @Value("\${messaging.consumer.visibility.extensionInterval:15}") private val visibilityExtensionInterval: Long
): AwsNotificationListener(handler, objectMapper, taskScheduler, queueName, visibilityExtensionInterval)