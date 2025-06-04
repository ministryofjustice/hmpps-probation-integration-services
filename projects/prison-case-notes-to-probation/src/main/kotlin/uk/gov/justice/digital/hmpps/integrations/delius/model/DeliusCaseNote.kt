package uk.gov.justice.digital.hmpps.integrations.delius.model

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType
import uk.gov.justice.digital.hmpps.integrations.prison.UNKNOWN_LOCATION
import uk.gov.justice.digital.hmpps.model.StaffName
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

data class DeliusCaseNote(val header: CaseNoteHeader, val body: CaseNoteBody) {
    val urn = when (header.type) {
        CaseNoteHeader.Type.CaseNote -> header.getCaseNoteUrn()
        CaseNoteHeader.Type.ActiveAlert -> header.getAlertUrn(true)
        CaseNoteHeader.Type.InactiveAlert -> header.getAlertUrn(false)
    }

    companion object {
        const val CASE_NOTE_URN_PREFIX: String = "urn:uk:gov:hmpps:prison-case-note:"
        const val ALERT_URN_PREFIX: String = "urn:uk:gov:hmpps:prisoner-alert:"

        fun CaseNoteHeader.getAlertUrn(active: Boolean): String {
            val urnActive = if (active) "active" else "inactive"
            return "${ALERT_URN_PREFIX}${urnActive}:${uuid}"
        }

        fun CaseNoteHeader.getCaseNoteUrn(): String {
            return "${CASE_NOTE_URN_PREFIX}${uuid}"
        }
    }

    fun isOfInterestForExternalReference(): Boolean = when (header.type) {
        CaseNoteHeader.Type.CaseNote, CaseNoteHeader.Type.InactiveAlert -> body.contactTimeStamp.isAfter(
            ZonedDateTime.now().minusYears(1)
        )

        else -> true
    }
}

data class CaseNoteHeader(val nomisId: String, val legacyId: Long?, val uuid: UUID, val type: Type) {
    enum class Type {
        ActiveAlert, InactiveAlert, CaseNote
    }
}

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

    val alertDescription: String,
) {
    fun typeLookup() = "$type $subType"

    fun notes(length: Int = 0): String {
        val notes = typeLookup() + extraNotes() + System.lineSeparator() + content
        return notes.padEnd(length)
    }

    private fun extraNotes(): String = StringBuilder().apply {
        if (locationUnknown()) {
            append(System.lineSeparator())
            append("Establishment details were not available when this contact was created. Please refer to throughcare history for location details if required.")
        }
        if (alertDescription.isNotBlank()) {
            append(System.lineSeparator())
            append(alertDescription)
        }
    }.toString()

    fun locationUnknown(): Boolean = establishmentCode == UNKNOWN_LOCATION

    fun description(caseNoteType: CaseNoteType): String? {
        return when {
            typeLookup().isAlertType() -> "NOMIS $alertDescription".truncatedDescription()
            isResettlementPassport() -> BcstPathway.from(content)?.let { "BCST case note for $it" }
            caseNoteType.code == CaseNoteType.DEFAULT_CODE -> "NOMIS Case Note - $type - $subType"
            else -> null
        }
    }

    private fun String.truncatedDescription(): String {
        val bytes = this.toByteArray()
        var count = 0
        return if (bytes.size > 200) {
            takeWhile {
                count += it.toString().encodeToByteArray().size
                count < 199
            } + " ~"
        } else this
    }

    private fun isResettlementPassport() = type == "RESET" && subType == "BCST"
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
