package uk.gov.justice.digital.hmpps.publisher

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import io.awspring.cloud.sns.core.SnsTemplate
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.core.MessagePostProcessor
import org.springframework.messaging.support.MessageBuilder
import uk.gov.justice.digital.hmpps.message.MessageAttribute
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification

@ExtendWith(MockitoExtension::class)
class TopicPublisherTest {

    @Mock
    lateinit var notificationTemplate: SnsTemplate

    lateinit var publisher: NotificationPublisher

    @BeforeEach
    fun setup() {
        publisher = TopicPublisher(notificationTemplate, "my-topic")
    }

    @Test
    fun `can publish notification message`() {
        val json = JsonNodeFactory.instance.objectNode()
        json.put("Test", "value")
        val attrs = MessageAttributes("OFFENDER_UPDATED")
        attrs["anotherValue"] = MessageAttribute("String", "Test")
        val notification = Notification(json, attrs)

        val captor = ArgumentCaptor.forClass(MessagePostProcessor::class.java)

        publisher.publish(notification)

        verify(notificationTemplate).convertAndSend(eq("my-topic"), eq(notification.message), captor.capture())

        val message = captor.value.postProcessMessage(
            MessageBuilder.createMessage(notification.message, MessageHeaders(mapOf()))
        )

        assertThat(
            message.headers["eventType"],
            equalTo("OFFENDER_UPDATED")
        )
        assertThat(
            message.headers["anotherValue"],
            equalTo("Test")
        )
    }
}
