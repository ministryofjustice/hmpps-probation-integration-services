package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.util.ResourceUtils
import uk.gov.justice.digital.hmpps.integrations.nomis.CaseNoteMessage
import uk.gov.justice.digital.hmpps.integrations.nomis.NomisCaseNote
import uk.gov.justice.digital.hmpps.listener.CaseNoteMessageWrapper
import java.nio.file.Paths.get

object ResourceLoader {

    val resourceLocationStr: String

    init {
        val resource = ResourceUtils.getFile("classpath:application-dev.yml")
        resourceLocationStr = resource.parent
    }

    private val MAPPER = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerKotlinModule()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun caseNoteMessage(filename: String): CaseNoteMessage =
        MAPPER.readValue(
            get("$resourceLocationStr/messages/$filename.json").toFile(),
            CaseNoteMessageWrapper::class.java
        ).message

    fun nomisCaseNote(filename: String): NomisCaseNote =
        MAPPER.readValue(
            get("$resourceLocationStr/responses/$filename.json").toFile(),
            NomisCaseNote::class.java
        )
}
