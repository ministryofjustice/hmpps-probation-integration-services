package uk.gov.justice.digital.hmpps.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.listener.AsyncAdapterBlockingExecutionFailedException
import io.awspring.cloud.sqs.listener.ListenerExecutionFailedException
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.annotations.SpanAttribute
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.sentry.Sentry
import io.sentry.spring.jakarta.tracing.SentryTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.extractSpanContext
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.startSpan
import java.util.concurrent.CompletionException

@Component
@Conditional(AwsCondition::class)
@ConditionalOnExpression("\${messaging.consumer.enabled:true} and '\${messaging.consumer.queue:}' != ''")
class AwsNotificationListener(
    private val handler: NotificationHandler<*>,
    private val objectMapper: ObjectMapper
) {
    @WithSpan(kind = SpanKind.CONSUMER)
    @SentryTransaction(operation = "messaging")
    @SqsListener("\${messaging.consumer.queue}")
    fun receive(@SpanAttribute message: String) {
        val attributes = objectMapper.readValue(message, jacksonTypeRef<Notification<String>>()).attributes
        val span = attributes.extractSpanContext().startSpan(this::class.java.name, "receive", SpanKind.CONSUMER)
        span.makeCurrent().use {
            try {
                retry(3, RETRYABLE_EXCEPTIONS) { handler.handle(message) }
            } catch (e: Throwable) {
                Sentry.captureException(unwrapSqsExceptions(e))
                throw e
            }
        }
        span.end()
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
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
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
