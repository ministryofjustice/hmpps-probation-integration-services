package uk.gov.justice.digital.hmpps.integrations.delius.model

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.ZonedDateTime

data class DeliusCaseNote(val header: CaseNoteHeader, val body: CaseNoteBody)

data class CaseNoteHeader(val nomisId: String, val noteId: Long)

data class CaseNoteBody(
    @NotBlank
    val type: String,
    @NotBlank
    val subType: String,
    @NotBlank
    val content: String,
    @NotNull
    val contactTimeStamp: ZonedDateTime,
    @NotNull
    val systemTimestamp: ZonedDateTime,
    @Valid
    val staffName: StaffName,
    @NotBlank
    val establishmentCode: String,
) {
    fun typeLookup() = "$type $subType"

    fun notes(length: Int = 0): String {
        val notes = typeLookup() + System.lineSeparator() + content
        return notes.padEnd(length)
    }
}

data class StaffName(
    @NotBlank val forename: String,
    @NotBlank val surname: String,
)
