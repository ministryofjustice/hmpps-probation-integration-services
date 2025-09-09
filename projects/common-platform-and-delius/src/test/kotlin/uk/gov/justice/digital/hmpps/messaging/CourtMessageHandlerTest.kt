package uk.gov.justice.digital.hmpps.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.operations.SqsTemplate
import io.sentry.Sentry
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mockStatic
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.set
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class CourtMessageHandlerTest {
    @Mock
    lateinit var converter: NotificationConverter<CommonPlatformHearing>

    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var sqsTemplate: SqsTemplate

    @Mock
    lateinit var objectMapper: ObjectMapper

    lateinit var handler: CourtMessageHandler

    @BeforeEach
    fun setup() {
        handler = CourtMessageHandler(
            converter,
            telemetryService,
            sqsTemplate,
            MockMvcExtensions.objectMapper,
            "receive-queue",
            "send-queue"
        )
    }

    @Test
    fun `message with multiple defendants is split into multiple messages`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_MULTIPLE_DEFENDANTS)
        val incomingMessage = Optional.of(
            MessageBuilder.createMessage(
                MockMvcExtensions.objectMapper.writeValueAsString(notification),
                MessageHeaders(mapOf())
            )
        )

        whenever(sqsTemplate.receive(any<String>(), any<Class<String>>()))
            .thenReturn(incomingMessage)
        whenever(converter.fromMessage(any())).thenReturn(notification)

        handler.handle()

        verify(sqsTemplate, Mockito.times(2)).send(any(), any<Message<String>>())
    }

    @Test
    fun `message with multiple defendants output only contains single defendant`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_MULTIPLE_DEFENDANTS)
        val incomingMessage = Optional.of(
            MessageBuilder.createMessage(
                MockMvcExtensions.objectMapper.writeValueAsString(notification),
                MessageHeaders(mapOf())
            )
        )

        whenever(sqsTemplate.receive(any<String>(), any<Class<String>>()))
            .thenReturn(incomingMessage)
        whenever(converter.fromMessage(any())).thenReturn(notification)

        handler.handle()

        val captor = argumentCaptor<Message<String>>()
        verify(sqsTemplate, times(2)).send(eq("send-queue"), captor.capture())

        val output = MockMvcExtensions.objectMapper.readValue(
            captor.firstValue.payload,
            Notification::class.java
        ) as Notification<String>
        val outputHearing = MockMvcExtensions.objectMapper.readValue(output.message, CommonPlatformHearing::class.java)

        assertThat(outputHearing.hearing.prosecutionCases.size, equalTo(1))
        assertThat(outputHearing.hearing.prosecutionCases[0].defendants.size, equalTo(1))
    }

    @Test
    fun `should send sentry alert if last message received over an hour ago`() {
        mockStatic(Sentry::class.java).use {
            val now = LocalDateTime.now()
            handler.set("lastReceivedMessageTime", now.minusHours(2))

            handler.checkMessageInactivity()

            it.verify { Sentry.captureEvent(any()) }
        }
    }

    @Test
    fun `should not send sentry alert if last message was less than an hour ago`() {
        mockStatic(Sentry::class.java).use {
            val now = LocalDateTime.now()
            handler.set("lastReceivedMessageTime", now.minusMinutes(30))

            handler.checkMessageInactivity()

            it.verify({ Sentry.captureEvent(any()) },
                times(0))
        }
    }

    @Test
    fun `should not send alert outside business hours`() {
        val localDateTimeMock = mockStatic(LocalDateTime::class.java, Mockito.CALLS_REAL_METHODS)
        val date = LocalDateTime.of(2025, 9, 7, 3, 0)

        mockStatic(Sentry::class.java).use {
            localDateTimeMock.`when`<LocalDateTime> { LocalDateTime.now() }
                .thenReturn(date)

            val now = LocalDateTime.now()
            handler.set("lastReceivedMessageTime", now.minusHours(2))

            handler.checkMessageInactivity()
            it.verify({ Sentry.captureEvent(any()) },
                times(0))
        }

        localDateTimeMock.close()
    }
}