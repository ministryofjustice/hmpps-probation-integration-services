package uk.gov.justice.digital.hmpps.converter

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.datetime.ZonedDateTimeDeserializer
import uk.gov.justice.digital.hmpps.message.AdditionalInformation
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class HmppsDomainEventConverterTest {
    private val mapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerKotlinModule()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(SimpleModule().addDeserializer(ZonedDateTime::class.java, ZonedDateTimeDeserializer()))

    private val converter = HmppsDomainEventConverter(mapper)

    @Test
    fun `can convert from jms message`() {
        val message =
            """
                {
                  "Type": "Notification",
                  "MessageId": "20d491ef-1213-4c50-92a7-96977ee66abd",
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

        val event: HmppsDomainEvent = converter.fromMessage(message).message
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

        val attributes: MessageAttributes = converter.fromMessage(message).attributes
        assertThat(attributes["eventType"]!!.value, equalTo("attribute.event.type"))
    }

    @Test
    fun `can convert to jms message`() {
        val hmppsEvent = Notification(
            message = HmppsDomainEvent(
                "message.event.type",
                1,
                "http://detail/url",
                ZonedDateTime.parse("2022-07-27T15:22:08.509+01:00"),
                "A description for the event",
                AdditionalInformation(mutableMapOf("specialId" to "6aafe304-861f-4479-8380-fec5f90f6d17")),
                PersonReference(listOf(PersonIdentifier("CRN", "X123456")))
            ),
            attributes = MessageAttributes("attribute.event.type")
        )

        val message = converter.toMessage(hmppsEvent)

        assertThat(
            message,
            equalTo(
                """
                |{"Message":"{\"eventType\":\"message.event.type\",\"version\":1,\"detailUrl\":\"http://detail/url\",\"occurredAt\":\"2022-07-27T15:22:08.509+01:00\",\"description\":\"A description for the event\",\"personReference\":{\"identifiers\":[{\"type\":\"CRN\",\"value\":\"X123456\"}]},\"additionalInformation\":{\"specialId\":\"6aafe304-861f-4479-8380-fec5f90f6d17\"}}",
                |"MessageAttributes":{"eventType":{"Type":"String","Value":"attribute.event.type"}},"MessageId":"${hmppsEvent.id}"}
                """.trimMargin().replace("\\n".toRegex(), "")
            )
        )
    }
}
