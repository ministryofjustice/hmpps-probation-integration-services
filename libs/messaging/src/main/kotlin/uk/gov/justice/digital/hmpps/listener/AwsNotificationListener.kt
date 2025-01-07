package uk.gov.justice.digital.hmpps.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.listener.AsyncAdapterBlockingExecutionFailedException
import io.awspring.cloud.sqs.listener.ListenerExecutionFailedException
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.sentry.Sentry
import io.sentry.spring.jakarta.tracing.SentryTransaction
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Conditional
import org.springframework.dao.CannotAcquireLockException
import org.springframework.jdbc.CannotGetJdbcConnectionException
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Component
import org.springframework.transaction.CannotCreateTransactionException
import org.springframework.transaction.UnexpectedRollbackException
import org.springframework.web.client.RestClientException
import uk.gov.justice.digital.hmpps.config.AwsCondition
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.NotificationHandler
import uk.gov.justice.digital.hmpps.retry.retry
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.extractTelemetryContext
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.withSpan
import java.util.concurrent.CompletionException

@Component
@Conditional(AwsCondition::class)
@ConditionalOnExpression("\${messaging.consumer.enabled:true} and '\${messaging.consumer.queue:}' != ''")
class AwsNotificationListener(
    private val handler: NotificationHandler<*>,
    private val objectMapper: ObjectMapper,
    @Value("\${messaging.consumer.queue}") private val queueName: String
) {
    @SentryTransaction(operation = "messaging")
    @SqsListener("\${messaging.consumer.queue}")
    fun receive(message: String) {
        val notification = objectMapper.readValue(message, jacksonTypeRef<Notification<String>>())
        notification.attributes
            .extractTelemetryContext()
            .withSpan(
                this::class.java.simpleName,
                "RECEIVE ${notification.eventType ?: "unknown event type"}",
                SpanKind.CONSUMER
            ) {
                Span.current().setAttribute("queue", queueName)
                try {
                    retry(3, RETRYABLE_EXCEPTIONS) { handler.handle(message) }
                } catch (t: Throwable) {
                    val e = unwrapSqsExceptions(t)
                    Span.current().recordException(e).setStatus(StatusCode.ERROR)
                    Sentry.captureException(e)
                    throw t
                }
            }
    }

    fun unwrapSqsExceptions(e: Throwable): Throwable {
        fun unwrap(e: Throwable) = e.cause ?: e
        var cause = e
        if (cause is CompletionException) {
            cause = unwrap(cause)
        }
        if (cause is AsyncAdapterBlockingExecutionFailedException) {
            cause = unwrap(cause)
        }
        if (cause is ListenerExecutionFailedException) {
            cause = unwrap(cause)
        }
        return cause
    }

    companion object {
        val RETRYABLE_EXCEPTIONS = listOf(
            RestClientException::class,
            CannotAcquireLockException::class,
            ObjectOptimisticLockingFailureException::class,
            CannotCreateTransactionException::class,
            CannotGetJdbcConnectionException::class,
            UnexpectedRollbackException::class
        )
    }
}
