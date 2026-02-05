package uk.gov.justice.digital.hmpps.datetime

import org.springframework.boot.jackson.JacksonComponent
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

@JacksonComponent
class ZonedDateTimeDeserializer : ValueDeserializer<ZonedDateTime>() {
    companion object {
        val formatter: DateTimeFormatter = DateTimeFormatterBuilder().parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .parseLenient()
            .optionalStart()
            .appendOffsetId()
            .parseStrict()
            .optionalStart()
            .appendLiteral('[')
            .parseCaseSensitive()
            .appendZoneRegionId()
            .appendLiteral(']')
            .optionalEnd()
            .optionalEnd()
            .toFormatter()

        fun deserialize(text: String): ZonedDateTime {
            val datetime = formatter.parseBest(text, ZonedDateTime::from, LocalDateTime::from)
            return if (datetime is ZonedDateTime) {
                datetime.withZoneSameInstant(EuropeLondon)
            } else {
                (datetime as LocalDateTime).atZone(EuropeLondon)
            }
        }
    }

    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext?) = deserialize(parser.string)
}
