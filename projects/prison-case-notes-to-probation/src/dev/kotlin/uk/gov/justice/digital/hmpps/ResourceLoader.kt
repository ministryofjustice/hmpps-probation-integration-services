package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.util.ResourceUtils
import uk.gov.justice.digital.hmpps.datetime.ZonedDateTimeDeserializer
import uk.gov.justice.digital.hmpps.integrations.prison.CaseNoteMessage
import uk.gov.justice.digital.hmpps.integrations.prison.PrisonCaseNote
import uk.gov.justice.digital.hmpps.listener.CaseNoteMessageWrapper
import java.time.ZonedDateTime

object ResourceLoader {

    private val MAPPER = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerKotlinModule()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(SimpleModule().addDeserializer(ZonedDateTime::class.java, ZonedDateTimeDeserializer()))

    fun caseNoteMessage(filename: String): CaseNoteMessage =
        MAPPER.readValue(
            MAPPER.readValue(
                ResourceUtils.getFile("classpath:messages/$filename.json"),
                CaseNoteMessageWrapper::class.java
            ).message,
            CaseNoteMessage::class.java
        )

    fun nomisCaseNote(filename: String): PrisonCaseNote =
        MAPPER.readValue(
            ResourceUtils.getFile("classpath:responses/$filename.json"),
            PrisonCaseNote::class.java
        )
}
