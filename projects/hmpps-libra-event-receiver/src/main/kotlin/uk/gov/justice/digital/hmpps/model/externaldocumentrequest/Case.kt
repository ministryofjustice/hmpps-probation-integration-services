package uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.OptBoolean
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Case(
    @JacksonXmlProperty(localName = "c_id")
    @get:JsonProperty("cId")
    val cId: String? = null,
    @field:NotBlank
    @JacksonXmlProperty(localName = "caseno")
    val caseNo: String?,
    @JacksonXmlProperty(localName = "cseq")
    @JsonIgnore
    val seq: Int? = null,
    @JacksonXmlProperty(localName = "def_name_elements")
    val name: Name? = null,
    @JacksonXmlProperty(localName = "def_name")
    val defendantName: String? = null,
    @JacksonXmlProperty(localName = "def_type")
    val defendantType: String? = null,
    @JacksonXmlProperty(localName = "def_sex")
    val defendantSex: String? = null,
    @JacksonXmlProperty(localName = "def_dob")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", lenient = OptBoolean.TRUE)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val defendantDob: LocalDate? = null,
    @JacksonXmlProperty(localName = "def_addr")
    val defendantAddress: Address? = null,
    @JacksonXmlProperty(localName = "def_age")
    @JsonIgnore
    val defendantAge: Int? = null,
    @JacksonXmlProperty(localName = "cro_number")
    val cro: String? = null,
    @JacksonXmlProperty(localName = "pnc_id")
    val pnc: String? = null,
    @JacksonXmlProperty(localName = "listno")
    val listNo: String? = null,
    @JacksonXmlProperty(localName = "urn")
    val urn: String? = null,
    @JacksonXmlProperty(localName = "nationality_1")
    val nationality1: String? = null,
    @JacksonXmlProperty(localName = "nationality_2")
    val nationality2: String? = null,
    @field:NotNull
    @field:Valid
    @JacksonXmlProperty(localName = "offences")
    @JacksonXmlElementWrapper
    val offences: List<Offence> = ArrayList(),
) {
    @field:Valid
    @field:NotNull
    @JsonBackReference
    var block: Block = Block(-1L)

    val courtRoom: String?
        @JsonGetter
        get() = (block.session.courtRoom)

    val courtCode: String
        @JsonGetter
        get() = (block.session.courtCode)

    val sessionStartTime: LocalDateTime
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
        @JsonGetter
        get() = (block.session.getSessionStartTime())
}
