package uk.gov.justice.digital.hmpps.listener

import feign.FeignException
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.listener.AsyncAdapterBlockingExecutionFailedException
import io.awspring.cloud.sqs.listener.ListenerExecutionFailedException
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.sentry.Sentry
import io.sentry.spring.jakarta.tracing.SentryTransaction
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Conditional
import org.springframework.dao.CannotAcquireLockException
import org.springframework.jdbc.CannotGetJdbcConnectionException
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Component
import org.springframework.transaction.CannotCreateTransactionException
import uk.gov.justice.digital.hmpps.config.AwsCondition
import uk.gov.justice.digital.hmpps.messaging.NotificationHandler
import uk.gov.justice.digital.hmpps.retry.retry
import java.util.concurrent.CompletionException

@Component
@Conditional(AwsCondition::class)
@ConditionalOnExpression("\${messaging.consumer.enabled:true} and '\${messaging.consumer.queue:}' != ''")
class AwsNotificationListener(
    private val handler: NotificationHandler<*>
) {
    @SqsListener("\${messaging.consumer.queue}")
    @SentryTransaction(operation = "messaging")
    @WithSpan(kind = SpanKind.CONSUMER)
    fun receive(message: String) {
        try {
            retry(
                3,
                listOf(
                    FeignException.NotFound::class,
                    CannotAcquireLockException::class,
                    ObjectOptimisticLockingFailureException::class,
                    CannotCreateTransactionException::class,
                    CannotGetJdbcConnectionException::class
                )
            ) { handler.handle(message) }
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
