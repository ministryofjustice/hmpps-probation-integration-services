package uk.gov.justice.digital.hmpps.messaging

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.awspring.cloud.sqs.operations.SqsTemplate
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.objectMapper
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class CourtMessageHandlerTest {
    @Mock
    lateinit var converter: NotificationConverter<CommonPlatformHearing>

    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var sqsTemplate: SqsTemplate

    lateinit var handler: CourtMessageHandler

    @BeforeEach
    fun setup() {
        handler = CourtMessageHandler(
            converter,
            telemetryService,
            sqsTemplate,
            objectMapper,
            "receive-queue",
            "send-queue"
        )
    }

    @Test
    fun `message with multiple defendants is split into multiple messages`() {
        val notification = Notification(message = MessageGenerator.COMMON_PLATFORM_EVENT_MULTIPLE_DEFENDANTS)
        val incomingMessage = Optional.of(
            MessageBuilder.createMessage(
                objectMapper.writeValueAsString(notification),
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
                objectMapper.writeValueAsString(notification),
                MessageHeaders(mapOf())
            )
        )

        whenever(sqsTemplate.receive(any<String>(), any<Class<String>>()))
            .thenReturn(incomingMessage)
        whenever(converter.fromMessage(any())).thenReturn(notification)

        handler.handle()

        val captor = argumentCaptor<Message<String>>()
        verify(sqsTemplate, times(2)).send(eq("send-queue"), captor.capture())

        val output = objectMapper.readValue(captor.firstValue.payload, jacksonTypeRef<Notification<String>>())
        val outputHearing = objectMapper.readValue(output.message, CommonPlatformHearing::class.java)

        assertThat(outputHearing.hearing.prosecutionCases.size, equalTo(1))
        assertThat(outputHearing.hearing.prosecutionCases[0].defendants.size, equalTo(1))
    }
}