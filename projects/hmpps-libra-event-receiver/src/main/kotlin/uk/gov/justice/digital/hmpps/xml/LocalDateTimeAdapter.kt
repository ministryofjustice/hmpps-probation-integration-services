package uk.gov.justice.digital.hmpps.xml

import jakarta.xml.bind.annotation.adapters.XmlAdapter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeAdapter : XmlAdapter<CharSequence, LocalDateTime>() {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss")

    override fun unmarshal(value: CharSequence): LocalDateTime = LocalDateTime.parse(value)

    override fun marshal(value: LocalDateTime): String = value.format(this.dateTimeFormatter)
}