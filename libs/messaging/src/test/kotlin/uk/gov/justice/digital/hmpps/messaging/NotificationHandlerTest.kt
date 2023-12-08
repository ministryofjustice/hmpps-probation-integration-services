package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification

@ExtendWith(MockitoExtension::class)
class NotificationHandlerTest {
    @Mock
    lateinit var domainConverter: NotificationConverter<HmppsDomainEvent>

    @Test
    fun `handle string calls converter`() {
        val event = HmppsDomainEvent("test.event.type", 1)
        whenever(domainConverter.fromMessage(any())).thenReturn(
            Notification(event, MessageAttributes(event.eventType)),
        )
        val handler =
            object : NotificationHandler<HmppsDomainEvent> {
                override val converter = domainConverter

                override fun handle(notification: Notification<HmppsDomainEvent>) {
                    assertThat(notification.eventType, equalTo("test.event.type"))
                }
            }
        handler.handle("{}")
        verify(domainConverter).fromMessage("{}")
    }
}
