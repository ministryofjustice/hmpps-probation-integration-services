package uk.gov.justice.digital.hmpps.listener

import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.listener.AsyncAdapterBlockingExecutionFailedException
import io.awspring.cloud.sqs.listener.ListenerExecutionFailedException
import io.sentry.Sentry
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.config.AwsCondition
import uk.gov.justice.digital.hmpps.messaging.NotificationHandler
import java.util.concurrent.CompletionException

@Component
@Conditional(AwsCondition::class)
@ConditionalOnProperty("messaging.consumer.queue")
class AwsNotificationListener(
    private val handler: NotificationHandler<*>
) {
    @SqsListener("\${messaging.consumer.queue}")
    fun receive(message: String) {
        try {
            handler.handle(message)
        } catch (e: Throwable) {
            Sentry.captureException(unwrapSqsExceptions(e))
            throw e
        }
    }

    fun unwrapSqsExceptions(e: Throwable): Throwable {
        fun unwrap(e: Throwable) = e.cause ?: e
        var cause = e
        if (cause is CompletionException) { cause = unwrap(cause) }
        if (cause is AsyncAdapterBlockingExecutionFailedException) { cause = unwrap(cause) }
        if (cause is ListenerExecutionFailedException) { cause = unwrap(cause) }
        return cause
    }
}
