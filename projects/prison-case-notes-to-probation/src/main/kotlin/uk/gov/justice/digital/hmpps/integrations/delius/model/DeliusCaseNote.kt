package uk.gov.justice.digital.hmpps.integrations.delius.model

import java.time.ZonedDateTime
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import kotlin.math.abs

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
    val establishmentCode: String
) {
    fun typeLookup() = "$type $subType"
    fun notes(length: Int = 0): String {
        val notes = typeLookup() + System.lineSeparator() + content
        return notes.padEnd(abs(length - notes.length) + notes.length)
    }
}

data class StaffName(@NotBlank val forename: String, @NotBlank val surname: String)
