package uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.util.Objects

data class Info(
    @Valid
    @Positive
    @JsonIgnore
    @NotNull
    val sequence: Long,
    @field:NotBlank
    @field:Size(min = 5, message = "Invalid ou code")
    @JsonIgnore
    val ouCode: String,
    @field:NotNull
    @JsonIgnore
    val dateOfHearing: LocalDate,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as Info
        return ouCode == that.ouCode && Objects.equals(dateOfHearing, that.dateOfHearing)
    }

    override fun hashCode(): Int = Objects.hash(ouCode, dateOfHearing)

    companion object {
        const val SOURCE_FILE_NAME_ELEMENT = "source_file_name"
    }
}
