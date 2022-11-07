package uk.gov.justice.digital.hmpps.publisher

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import io.awspring.cloud.messaging.core.NotificationMessagingTemplate
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
class AwsNotificationPublisherTest {

    @Mock
    lateinit var notificationTemplate: NotificationMessagingTemplate

    lateinit var publisher: NotificationPublisher

    @BeforeEach
    fun setup() {
        publisher = AwsNotificationPublisher(notificationTemplate)
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

        verify(notificationTemplate).convertAndSend(
            eq(notification.message), captor.capture()
        )
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