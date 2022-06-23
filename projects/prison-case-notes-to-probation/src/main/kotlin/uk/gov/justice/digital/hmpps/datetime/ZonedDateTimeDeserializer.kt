package uk.gov.justice.digital.hmpps.datetime

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.springframework.boot.jackson.JsonComponent
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@JsonComponent
class ZonedDateTimeDeserializer : JsonDeserializer<ZonedDateTime>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext?): ZonedDateTime {
        val json = parser.text
        return if (json.contains(".*(\\+|-)\\d{2}(:?\\d{2})".toRegex())) {
            ZonedDateTime.parse(json).withZoneSameInstant(ZoneId.systemDefault())
        } else {
            ZonedDateTime.of(LocalDateTime.parse(json), ZoneId.systemDefault())
        }
    }
}
