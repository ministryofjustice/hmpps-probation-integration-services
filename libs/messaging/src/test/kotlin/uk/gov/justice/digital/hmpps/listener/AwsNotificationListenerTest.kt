package uk.gov.justice.digital.hmpps.listener

import io.awspring.cloud.sqs.SqsException
import io.awspring.cloud.sqs.listener.AsyncAdapterBlockingExecutionFailedException
import io.awspring.cloud.sqs.listener.ListenerExecutionFailedException
import io.awspring.cloud.sqs.listener.Visibility
import io.sentry.Sentry
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.messaging.support.GenericMessage
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.NotificationHandler
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.objectMapper
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.concurrent.CompletionException

@ExtendWith(MockitoExtension::class)
class AwsNotificationListenerTest {
    @Mock
    lateinit var handler: NotificationHandler<Any>

    lateinit var listener: AwsNotificationListener

    @BeforeEach
    fun setUp() {
        listener =
            AwsNotificationListener(handler, objectMapper, SimpleAsyncTaskScheduler(), "my-queue", 1)
    }

    @Test
    fun `messages are dispatched to handler`() {
        val notification = objectMapper.writeValueAsString(Notification("message"))
        listener.receive(notification)
        verify(handler).handle(notification)
    }

    @Test
    fun `errors are captured and rethrown`() {
        mockStatic(Sentry::class.java).use {
            val exception = RuntimeException("error")
            whenever(handler.handle(any<String>())).thenThrow(exception)

            assertThat(
                assertThrows<RuntimeException> {
                    listener.receive(objectMapper.writeValueAsString(Notification("message")))
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
            whenever(handler.handle(any<String>())).thenThrow(wrappedException)

            assertThat(
                assertThrows<CompletionException> {
                    listener.receive(objectMapper.writeValueAsString(Notification("message")))
                },
                equalTo(wrappedException)
            )

            it.verify { Sentry.captureException(meaningfulException) }
        }
    }

    @Test
    fun `visibility timeout is extended while running`() {
        val notification = objectMapper.writeValueAsString(Notification("message"))
        val visibility = mock(Visibility::class.java)
        whenever(visibility.changeToAsync(30)).thenReturn(completedFuture(null))
        whenever(handler.handle(any<String>())).thenAnswer { Thread.sleep(1500) }

        listener.receive(notification, visibility)

        verify(visibility, times(2)).changeToAsync(30)
        verify(handler).handle(notification)
    }

    @Test
    fun `failure to change visibility timeout is ignored`() {
        val notification = objectMapper.writeValueAsString(Notification("message"))
        val visibility = mock(Visibility::class.java)
        whenever(visibility.changeToAsync(30)).thenReturn(runAsync { throw SqsException("error", null) })

        assertDoesNotThrow {
            listener.receive(notification, visibility)
        }
    }
}
