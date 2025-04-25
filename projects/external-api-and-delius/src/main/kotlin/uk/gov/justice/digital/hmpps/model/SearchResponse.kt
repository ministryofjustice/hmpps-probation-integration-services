package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate
import java.time.Period

data class ProbationCaseDetail(
    val otherIds: OtherIds,
    val firstName: String,
    val surname: String,
    val dateOfBirth: LocalDate?,
    val gender: String,
    val middleNames: List<String> = listOf(),
    val offenderProfile: CaseProfile = CaseProfile(),
    val contactDetails: ContactDetails? = ContactDetails(),
    val offenderAliases: List<CaseAlias> = listOf(),
    val activeProbationManagedSentence: Boolean = false,
    val currentRestriction: Boolean = false,
    val restrictionMessage: String? = null,
    val currentExclusion: Boolean = false,
    val exclusionMessage: String? = null,
) {
    val age: Int get() = Period.between(dateOfBirth, LocalDate.now()).years
}

data class CaseProfile(
    val ethnicity: String? = null,
    val nationality: String? = null,
    val religion: String? = null,
    val sexualOrientation: String? = null,
    val disabilities: List<CaseDisability> = emptyList(),
)

data class CaseAlias(
    val firstName: String?,
    val surname: String?,
    var dateOfBirth: LocalDate?,
    val gender: String?,
    val middleNames: List<String> = listOf(),
)

data class ContactDetails(
    val phoneNumbers: List<PhoneNumber> = listOf(),
    val emailAddresses: List<String> = listOf(),
)

data class PhoneNumber(
    val number: String,
    val type: String,
)

data class OtherIds(
    val crn: String,
    val pncNumber: String? = null,
    val nomsNumber: String? = null,
    val croNumber: String? = null,
)

data class CaseDisability(
    val disabilityType: CodedValue,
    val condition: CodedValue? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val notes: String? = null,
)

data class CodedValue(val code: String, val description: String)
