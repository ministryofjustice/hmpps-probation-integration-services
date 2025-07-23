package uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.OptBoolean
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class Session(
    @JacksonXmlProperty(localName = "s_id")
    val id: Long?,
    @field:NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", lenient = OptBoolean.TRUE)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    @JacksonXmlProperty(localName = "doh")
    val dateOfHearing: LocalDate?,
    @JacksonXmlProperty(localName = "court")
    val courtName: String?,
    @JacksonXmlProperty(localName = "room")
    val courtRoom: String?,
    @field:NotNull
    @JsonDeserialize(using = LocalTimeDeserializer::class)
    @JacksonXmlProperty(localName = "sstart")
    val start: LocalTime?,
    @JsonDeserialize(using = LocalTimeDeserializer::class)
    @JacksonXmlProperty(localName = "send")
    val end: LocalTime?,
    @JsonDeserialize(using = OuCodeDeserializer::class)
    @JacksonXmlProperty(localName = "ou_code")
    val ouCode: String?,
) {
    constructor(dateOfHearing: LocalDate, start: LocalTime) : this(null, dateOfHearing, null, null, start, null, null)
    constructor(ouCode: String) : this(null, null, null, null, null, null, ouCode)

    @field:NotNull
    @field:Valid
    @JacksonXmlElementWrapper
    @JsonManagedReference
    val blocks: List<Block> = ArrayList()

    @JsonBackReference
    lateinit var job: Job

    val courtCode: String
        get() = (ouCode ?: getCourtCodeFromInfo())

    private fun getCourtCodeFromInfo(): String {
        log.info("Retrieving courtCode from parent Info instance, court {}, room {} and date {}", job.dataJob.document.info.ouCode, courtRoom, dateOfHearing)
        return job.dataJob.document.info.ouCode
    }

    fun getSessionStartTime(): LocalDateTime = LocalDateTime.of(dateOfHearing, start)

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
