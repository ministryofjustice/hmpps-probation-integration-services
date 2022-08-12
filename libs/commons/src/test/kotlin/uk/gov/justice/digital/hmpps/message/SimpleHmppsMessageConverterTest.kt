package uk.gov.justice.digital.hmpps.message

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.datetime.ZonedDateTimeDeserializer
import java.time.ZonedDateTime
import javax.jms.BytesMessage
import javax.jms.Session
import javax.jms.TextMessage

@ExtendWith(MockitoExtension::class)
class SimpleHmppsMessageConverterTest {
    private val mapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerKotlinModule()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(SimpleModule().addDeserializer(ZonedDateTime::class.java, ZonedDateTimeDeserializer()))

    @Mock
    private lateinit var bytesMessage: BytesMessage

    @Mock
    private lateinit var textMessage: TextMessage

    @Mock
    private lateinit var session: Session

    private val converter = HmppsEventConverter(mapper)

    @Test
    fun `can convert from jms message`() {
        whenever(textMessage.text).thenReturn(
            """
                {
                  "Type": "Notification",
                  "MessageId": "aa2c2828-167f-529b-8e19-73735a2fb85c",
                  "TopicArn": "arn:aws:sns:eu-west-2:000000000000:QueueName",
                  "Message": "{\"eventType\":\"message.event.type\",\"version\":1,\"description\":\"A description for the event\",\"detailUrl\":\"http://detail/url\",\"occurredAt\":\"2022-07-27T15:22:08.509+01:00\",\"additionalInformation\":{\"specialId\":\"6aafe304-861f-4479-8380-fec5f90f6d17\"},\"personReference\":{\"identifiers\":[{\"type\":\"CRN\",\"value\":\"X123456\"}]}}",
                  "Timestamp": "2022-07-27T14:22:08.509Z",
                  "MessageAttributes": {
                    "eventType": {
                      "Type": "String",
                      "Value": "attribute.event.type"
                    }
                  }
                }
            """.trimIndent()
        )

        val event: HmppsEvent = converter.fromMessage(textMessage)
        assertThat(event.eventType, equalTo("message.event.type"))
        assertThat(event.version, equalTo(1))
        assertThat(event.description, equalTo("A description for the event"))
        assertThat(event.detailUrl, equalTo("http://detail/url"))
        assertThat(
            event.occurredAt,
            equalTo(ZonedDateTime.parse("2022-07-27T15:22:08.509+01:00").withZoneSameInstant(EuropeLondon))
        )
        assertThat(event.personReference.findCrn(), equalTo("X123456"))
        assertThat(event.additionalInformation["specialId"], equalTo("6aafe304-861f-4479-8380-fec5f90f6d17"))
    }

    @Test
    fun `can convert to jms message`() {
        val hmppsEvent = HmppsEvent(
            "message.event.type",
            1,
            "http://detail/url",
            ZonedDateTime.parse("2022-07-27T15:22:08.509+01:00"),
            "A description for the event",
            AdditionalInformation(mutableMapOf("specialId" to "6aafe304-861f-4479-8380-fec5f90f6d17")),
            PersonReference(listOf(PersonIdentifier("CRN", "X123456")))
        )

        whenever(session.createTextMessage()).thenReturn(textMessage)
        converter.toMessage(hmppsEvent, session)

        val messageJson = """
                |{"Message":"{\"eventType\":\"message.event.type\",\"version\":1,\"detailUrl\":\"http://detail/url\",\"occurredAt\":\"2022-07-27T15:22:08.509+01:00\",\"description\":\"A description for the event\",\"additionalInformation\":{\"specialId\":\"6aafe304-861f-4479-8380-fec5f90f6d17\"},\"personReference\":{\"identifiers\":[{\"type\":\"CRN\",\"value\":\"X123456\"}]}}",
                |"MessageAttributes":{"eventType":{"Type":"String","Value":"message.event.type"}}}
            """.trimMargin()

        verify(textMessage).text = messageJson.replace("\\n".toRegex(), "")
    }

    @Test
    fun `attempt to convert non hmmps event throws exception`() {
        val exception = assertThrows<IllegalArgumentException> { converter.toMessage("A simple string", session) }
        assertThat(
            "Unexpected event type passed to HmppsEventConverter: class kotlin.String",
            equalTo(exception.message)
        )
    }

    @Test
    fun `attempt to parse non text message throws exception`() {
        assertThrows<IllegalArgumentException> { converter.fromMessage(bytesMessage) }
    }
}
