package uk.gov.justice.digital.hmpps.integrations.delius.model

import java.time.ZonedDateTime
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

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
    @Valid
    val staffName: StaffName,
    @NotBlank
    val establishmentCode: String
) {
    fun notesToAppend() = "$type $subType" + System.lineSeparator() + content
}

data class StaffName(@NotBlank val forename: String, @NotBlank val surname: String)
