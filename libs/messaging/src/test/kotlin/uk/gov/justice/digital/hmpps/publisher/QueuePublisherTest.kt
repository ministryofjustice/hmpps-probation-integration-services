package uk.gov.justice.digital.hmpps.publisher

import io.awspring.cloud.sqs.operations.SqsTemplate
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.messaging.Message
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.objectMapper

@ExtendWith(MockitoExtension::class)
class QueuePublisherTest {

    @Mock
    lateinit var sqsTemplate: SqsTemplate

    @BeforeEach
    fun setup() {
        whenever(sqsTemplate.send<Notification<String>>(any(), any())).thenReturn(null)
    }

    @Test
    fun `publishes message to queue`() {
        val publisher = QueuePublisher(sqsTemplate, objectMapper, "my-queue", 1000)
        publisher.publish(Notification("test-message", MessageAttributes("test-event-type")))

        verify(sqsTemplate).send<String>(eq("my-queue"), check {
            assertThat(it.headers["eventType"], equalTo("test-event-type"))
            assertThat(it.message.asText(), equalTo("\"test-message\""))
        })
    }

    @Test
    fun `publishing rate is limited`() {
        val publisher = QueuePublisher(sqsTemplate, objectMapper, "my-queue", limit = 1)

        val notifications = List(10) { Notification(it, MessageAttributes("test-event-type")) }
        notifications.forEach { publisher.publish(it) }

        with(inOrder(sqsTemplate)) {
            for (i in 0..9) {
                verify(sqsTemplate).send<String>(eq("my-queue"), check {
                    assertThat(it.message.asInt(), equalTo(i))
                })
            }
            verifyNoMoreInteractions()
        }
    }

    private val Message<String>.message get() = objectMapper.readTree(payload).path("Message")
}
