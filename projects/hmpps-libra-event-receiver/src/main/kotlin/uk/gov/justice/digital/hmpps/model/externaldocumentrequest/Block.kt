package uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

data class Block(
    @field:NotNull
    @field:Valid
    @JacksonXmlProperty(localName = "sb_id")
    val id: Long,
) {
    @field:NotNull
    @field:Valid
    @JacksonXmlElementWrapper
    @JsonManagedReference
    val cases: List<Case> = ArrayList()

    @JsonBackReference
    lateinit var session: Session
}
