package uk.gov.justice.digital.hmpps.integrations.delius.model

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType
import uk.gov.justice.digital.hmpps.model.StaffName
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
    val establishmentCode: String
) {
    fun typeLookup() = "$type $subType"

    fun notes(length: Int = 0): String {
        val notes = typeLookup() + System.lineSeparator() + content
        return notes.padEnd(length)
    }

    fun description(caseNoteType: CaseNoteType): String? {
        return when {
            typeLookup().isAlertType() -> "NOMIS ${
                if (content.length > 194) "${content.take(192)} ~" else content.take(194)
            }"

            isResettlementPassport() -> BcstPathway.from(content)?.let { "BCST case note for $it" }
            caseNoteType.code == CaseNoteType.DEFAULT_CODE -> "NOMIS Case Note - $type - $subType"
            else -> null
        }
    }

    fun isResettlementPassport() = type == "RESET" && subType == "BCST"
}

fun String.isAlertType() = this == "ALERT ACTIVE" || this == "ALERT INACTIVE"

enum class BcstPathway(val keyword: String) {
    ACCOMMODATION("Accommodation"),
    ATTITUDES_THINKING_AND_BEHAVIOUR("Attitudes"),
    CHILDREN_FAMILIES_AND_COMMUNITY("Children"),
    DRUGS_AND_ALCOHOL("Drugs"),
    EDUCATION_SKILLS_AND_WORK("Education"),
    FINANCE_AND_ID("Finance"),
    HEALTH("Health");

    override fun toString(): String = when (this) {
        ACCOMMODATION -> "Accommodation"
        ATTITUDES_THINKING_AND_BEHAVIOUR -> "Attitudes, thinking and behaviour"
        CHILDREN_FAMILIES_AND_COMMUNITY -> "Children, families and community"
        DRUGS_AND_ALCOHOL -> "Drugs and alcohol"
        EDUCATION_SKILLS_AND_WORK -> "Education, skills and work"
        FINANCE_AND_ID -> "Finance and ID"
        HEALTH -> "Health"
    }

    companion object {
        private const val CASE_NOTE_PREFIX = "Case note summary from"
        private val PATHWAY_REGEX = Regex("$CASE_NOTE_PREFIX (\\w+)")
        fun from(content: String): BcstPathway? {
            val match = PATHWAY_REGEX.find(content)?.destructured?.component1()
            return match?.let { keyword ->
                entries.firstOrNull { it.keyword.equals(keyword, true) }
            }
        }
    }
}
