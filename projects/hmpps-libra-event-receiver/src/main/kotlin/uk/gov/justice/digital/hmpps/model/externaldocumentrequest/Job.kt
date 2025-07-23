package uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

data class Job(
    var name: String? = null,
) {
    constructor() : this ("")

    @field:Valid
    @field:NotNull
    @JacksonXmlElementWrapper
    @JsonManagedReference
    val sessions: MutableList<Session> = mutableListOf()

    @JsonBackReference
    lateinit var dataJob: DataJob
}
