package uk.gov.justice.digital.hmpps.listener

import io.awspring.cloud.sqs.listener.AsyncAdapterBlockingExecutionFailedException
import io.awspring.cloud.sqs.listener.ListenerExecutionFailedException
import io.sentry.Sentry
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mockStatic
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.messaging.support.GenericMessage
import uk.gov.justice.digital.hmpps.messaging.NotificationHandler
import java.util.concurrent.CompletionException

@ExtendWith(MockitoExtension::class)
class AwsNotificationListenerTest {
    @Mock
    lateinit var handler: NotificationHandler<Any>

    @InjectMocks
    lateinit var listener: AwsNotificationListener

    @Test
    fun `messages are dispatched to handler`() {
        listener.receive("message")
        verify(handler).handle("message")
    }

    @Test
    fun `errors are captured and rethrown`() {
        mockStatic(Sentry::class.java).use {
            val exception = RuntimeException("error")
            whenever(handler.handle("message")).thenThrow(exception)

            assertThat(
                assertThrows<RuntimeException> {
                    listener.receive("message")
                },
                equalTo(exception)
            )

            it.verify { Sentry.captureException(exception) }
        }
    }

    @Test
    fun `common SQS exceptions are unwrapped`() {
        mockStatic(Sentry::class.java).use {
            val meaningfulException = RuntimeException("error")
            val wrappedException = CompletionException(
                AsyncAdapterBlockingExecutionFailedException(
                    "async error",
                    ListenerExecutionFailedException("listener failure", meaningfulException, GenericMessage("test"))
                )
            )
            whenever(handler.handle("message")).thenThrow(wrappedException)

            assertThat(
                assertThrows<CompletionException> {
                    listener.receive("message")
                },
                equalTo(wrappedException)
            )

            it.verify { Sentry.captureException(meaningfulException) }
        }
    }
}
