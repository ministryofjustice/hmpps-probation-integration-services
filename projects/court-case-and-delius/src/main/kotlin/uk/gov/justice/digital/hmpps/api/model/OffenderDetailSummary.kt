package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate
import java.time.ZonedDateTime

data class OffenderDetailSummary(
    val preferredName: String?,
    val activeProbationManagedSentence: Boolean,
    val contactDetails: ContactDetails,
    val currentDisposal: String,
    val currentExclusion: Boolean,
    val currentRestriction: Boolean,
    val dateOfBirth: LocalDate,
    val firstName: String,
    val gender: String,
    val middleNames: List<String>,
    val offenderId: Long,
    val offenderProfile: OffenderProfile,
    val otherIds: OtherIds,
    val partitionArea: String,
    val previousSurname: String?,
    val softDeleted: Boolean,
    val surname: String,
    val title: String?
)

data class ContactDetails(
    val allowSMS: Boolean?,
    val emailAddresses: List<String>,
    val phoneNumbers: List<PhoneNumber>
)

data class OtherIds(
    val crn: String,
    val croNumber: String?,
    val immigrationNumber: String?,
    val mostRecentPrisonerNumber: String?,
    val niNumber: String?,
    val nomsNumber: String?,
    val pncNumber: String?
)

data class Disability(
    val lastUpdatedDateTime: ZonedDateTime,
    val disabilityCondition: KeyValue,
    val disabilityId: Long,
    val disabilityType: KeyValue,
    val endDate: LocalDate?,
    val isActive: Boolean,
    val notes: String?,
    val provisions: List<Provision>,
    val startDate: LocalDate
)

data class OffenderLanguages(
    val languageConcerns: String?,
    val otherLanguages: List<String> = emptyList(),
    val primaryLanguage: String?,
    val requiresInterpreter: Boolean?
)

data class OffenderProfile(
    val genderIdentity: String?,
    val selfDescribedGenderIdentity: String?,
    val disabilities: List<Disability> = emptyList(),
    val ethnicity: String?,
    val immigrationStatus: String?,
    val nationality: String?,
    val notes: String? = null,
    val offenderDetails: String?,
    val offenderLanguages: OffenderLanguages,
    val previousConviction: PreviousConviction?,
    val provisions: List<Provision> = emptyList(),
    val religion: String?,
    val remandStatus: String?,
    val riskColour: String?,
    val secondaryNationality: String?,
    val sexualOrientation: String?
)

data class PhoneNumber(
    val number: String?,
    val type: String
)

data class PreviousConviction(
    val convictionDate: LocalDate,
    val detail: Map<String, String>
)

data class Provision(
    val category: KeyValue?,
    val finishDate: LocalDate?,
    val notes: String?,
    val provisionId: Long,
    val provisionType: KeyValue,
    val startDate: LocalDate
)

enum class PhoneTypes {
    TELEPHONE,
    MOBILE
}
