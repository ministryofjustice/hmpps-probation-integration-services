package uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero

data class Offence(
    @field:NotNull
    @field:PositiveOrZero
    @JacksonXmlProperty(localName = "oseq")
    val seq: Int?,
    @JacksonXmlProperty(localName = "sum")
    val summary: String?,
    @JacksonXmlProperty(localName = "title")
    val title: String?,
    @JacksonXmlProperty(localName = "as")
    val act: String?,
    @JacksonXmlProperty(localName = "code")
    val code: String?,
)
