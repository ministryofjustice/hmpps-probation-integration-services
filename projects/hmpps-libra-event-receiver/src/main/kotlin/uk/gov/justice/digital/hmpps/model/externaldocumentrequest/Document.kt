package uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest

import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

data class Document(
    @field:Valid
    @field:NotNull
    @JsonDeserialize(using = DocumentInfoDeserializer::class)
    val info: Info,
) {
    @field:Valid
    @field:NotNull
    @JsonManagedReference
    val data: DataJob = DataJob()
}
