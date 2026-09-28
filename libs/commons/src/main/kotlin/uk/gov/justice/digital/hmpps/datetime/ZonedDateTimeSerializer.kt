package uk.gov.justice.digital.hmpps.datetime

import org.springframework.boot.jackson.JacksonComponent
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ser.std.StdSerializer
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@JacksonComponent
class ZonedDateTimeSerializer : StdSerializer<ZonedDateTime>(ZonedDateTime::class.java) {
    companion object {
        private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    }

    override fun serialize(value: ZonedDateTime, gen: JsonGenerator, ctxt: SerializationContext) {
        gen.writeString(formatter.format(value))
    }
}


