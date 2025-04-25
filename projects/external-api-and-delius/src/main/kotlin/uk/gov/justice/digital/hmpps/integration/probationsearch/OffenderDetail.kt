package uk.gov.justice.digital.hmpps.integration.probationsearch

import uk.gov.justice.digital.hmpps.integration.delius.entity.Disability
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.service.asCaseDisability
import java.time.LocalDate
import java.time.Period

data class OffenderDetail(
    val firstName: String = "",
    val middleNames: List<String>? = null,
    val surname: String = "",
    val dateOfBirth: LocalDate? = null,
    val gender: String? = null,
    val otherIds: IDs,
    val contactDetails: ContactDetails? = null,
    val offenderProfile: OffenderProfile? = null,
    val offenderAliases: List<OffenderAlias>? = null,
    val partitionArea: String? = null,
    val currentRestriction: Boolean? = null,
    val restrictionMessage: String? = null,
    val currentExclusion: Boolean? = null,
    val exclusionMessage: String? = null,
    val activeProbationManagedSentence: Boolean? = null
) {
    val age: Int? get() = dateOfBirth?.let { Period.between(it, LocalDate.now()).years }
}

data class IDs(
    val crn: String,
    val pncNumber: String? = null,
    val croNumber: String? = null,
    val nomsNumber: String? = null,
    val previousCrn: String? = null,
)

data class OffenderProfile(
    val ethnicity: String? = null,
    val nationality: String? = null,
    val notes: String? = null,
    val religion: String? = null,
    val sexualOrientation: String? = null,
    val disabilities: List<OffenderDisability>? = null,
)

data class OffenderAlias(
    val id: String? = null,
    val dateOfBirth: LocalDate? = null,
    val firstName: String? = null,
    val middleNames: List<String>? = null,
    val surname: String? = null,
    val gender: String? = null,
)

data class OffenderDisability(
    val disabilityType: CodedValue? = null,
    val condition: CodedValue? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val notes: String? = null,
)

fun IDs.ids() = OtherIds(crn, pncNumber, nomsNumber, croNumber)
fun OffenderDisability.asCaseDisability() = disabilityType?.let { type ->
    CaseDisability(
        CodedValue(type.code, type.description),
        this.condition?.let { CodedValue(it.code, it.description) },
        startDate,
        endDate,
        notes
    )
}

fun OffenderProfile.asProfile() = CaseProfile(
    ethnicity, nationality, religion, sexualOrientation,
    disabilities?.mapNotNull { it.asCaseDisability() } ?: emptyList())

fun OffenderAlias.asCaseAlias() = CaseAlias(firstName, surname, dateOfBirth, gender, middleNames ?: emptyList())