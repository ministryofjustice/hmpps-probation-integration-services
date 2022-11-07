package uk.gov.justice.digital.hmpps.publisher

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import io.awspring.cloud.messaging.core.NotificationMessagingTemplate
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
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
        val notification = Notification(json, MessageAttributes("OFFENDER_UPDATED"))

        val captor = ArgumentCaptor.forClass(MessagePostProcessor::class.java)

        publisher.publish(notification)

        verify(notificationTemplate).convertAndSend(
            eq(notification.message), captor.capture()
        )
        val message = captor.value.postProcessMessage(MessageBuilder.createMessage(notification.message, MessageHeaders(mapOf())))
        assertThat(message.headers.entries.map { Pair(it.key, it.value) },
            containsInAnyOrder("eventType" to "OFFENDER_UPDATED")
        )

    }
}