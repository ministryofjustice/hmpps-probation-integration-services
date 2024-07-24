package uk.gov.justice.digital.hmpps.api.model

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.LocalDate
import java.time.LocalDateTime

data class OffenderDetailSummary(
    val preferredName: String?,
    val activeProbationManagedSentence: Boolean,
    val contactDetails: ContactDetailsSummary,
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

data class OffenderDetail(
    val preferredName: String?,
    val activeProbationManagedSentence: Boolean,
    val contactDetails: ContactDetails,
    val currentDisposal: String,
    val currentExclusion: Boolean,
    val exclusionMessage: String?,
    val currentRestriction: Boolean,
    val restrictionMessage: String?,
    val dateOfBirth: LocalDate,
    val firstName: String,
    val gender: String,
    val middleNames: List<String>,
    val offenderId: Long,
    val offenderProfile: OffenderProfile,
    val offenderAliases: List<OffenderAlias>?,
    val offenderManagers: List<OffenderManager>,
    val otherIds: OtherIds,
    val partitionArea: String,
    val currentTier: String?,
    val previousSurname: String?,
    val softDeleted: Boolean,
    val surname: String,
    val title: String?
)

data class OffenderAlias(
    val id: String,
    val dateOfBirth: LocalDate?,
    val firstName: String,
    val middleNames: List<String>? = null,
    val surname: String,
    val gender: String
)

data class ContactDetailsSummary(
    val allowSMS: Boolean?,
    val emailAddresses: List<String>? = null,
    val phoneNumbers: List<PhoneNumber>? = null,
)

data class ContactDetails(
    val allowSMS: Boolean?,
    val emailAddresses: List<String>?,
    val phoneNumbers: List<PhoneNumber>?,
    val addresses: List<Address>?
)

data class Human(
    val forenames: String,
    val surname: String
)

data class StaffHuman(
    val code: String,
    val forenames: String,
    val surname: String,
    val unallocated: Boolean
)

data class Institution(
    val institutionId: Long,
    val isEstablishment: Boolean?,
    val code: String,
    val description: String,
    val institutionName: String?,
    val establishmentType: KeyValue?,
    val isPrivate: Boolean?,
    val nomsPrisonInstitutionCode: String? = null
)

data class ProbationArea(
    val probationAreaId: Long? = null,
    val code: String,
    val description: String,
    val organisation: KeyValue? = null,
    val institution: Institution? = null,
    val nps: Boolean,
    val teams: List<AllTeam>? = null
)

data class OffenderManager(
    val trustOfficer: Human,
    val staff: StaffHuman?,
    val providerEmployee: Human? = null,
    val partitionArea: String,
    val softDeleted: Boolean,
    val team: Team?,
    val probationArea: ProbationArea,
    val fromDate: LocalDate,
    val toDate: LocalDate? = null,
    val active: Boolean,
    val allocationReason: KeyValue? = null
)

data class Team(
    val code: String,
    val description: String,
    val telephone: String? = null,
    val emailAddress: String? = null,
    val localDeliveryUnit: KeyValue,
    val district: KeyValue,
    val borough: KeyValue,
    val teamType: KeyValue? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)

data class AllTeam(
    val providerTeamId: Long? = null,
    val teamId: Long? = null,
    val code: String,
    val description: String? = null,
    val name: String? = null,
    val isPrivate: Boolean? = null,
    val externalProvider: KeyValue? = null,
    val scProvider: KeyValue? = null,
    val localDeliveryUnit: KeyValue? = null,
    val district: KeyValue? = null,
    val borough: KeyValue? = null,
)

data class Address(
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val noFixedAbode: Boolean? = null,
    val notes: String? = null,
    val addressNumber: String? = null,
    val buildingName: String? = null,
    val streetName: String? = null,
    val district: String? = null,
    val town: String? = null,
    val county: String? = null,
    val postcode: String? = null,
    val telephoneNumber: String? = null,
    val status: KeyValue? = null,
    val type: KeyValue? = null,
    val typeVerified: Boolean? = null,
    val latestAssessmentDate: LocalDateTime? = null,
    val createdDatetime: LocalDateTime? = null,
    val lastUpdatedDatetime: LocalDateTime? = null,
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
    val lastUpdatedDateTime: LocalDateTime,
    val disabilityCondition: KeyValue,
    val disabilityId: Long,
    val disabilityType: KeyValue,
    val endDate: LocalDate?,
    val isActive: Boolean,
    val notes: String?,
    val provisions: List<Provision>? = null,
    val startDate: LocalDate
)

data class OffenderLanguages(
    val languageConcerns: String?,
    val otherLanguages: List<String>? = null,
    val primaryLanguage: String?,
    val requiresInterpreter: Boolean?
)

data class OffenderProfile(
    val genderIdentity: String?,
    val selfDescribedGender: String?,
    val disabilities: List<Disability>? = null,
    val ethnicity: String?,
    val immigrationStatus: String?,
    val nationality: String?,
    val notes: String? = null,
    val offenderDetails: String?,
    val offenderLanguages: OffenderLanguages,
    val previousConviction: PreviousConviction,
    val provisions: List<Provision>? = null,
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
    val convictionDate: LocalDate?,
    val detail: Map<String, String>?
)

data class Provision(
    val category: KeyValue?,
    val finishDate: LocalDate?,
    val notes: String?,
    val provisionId: Long,
    val provisionType: KeyValue,
    val startDate: LocalDate
)

@ConfigurationProperties(prefix = "lao-access")
data class LaoAccess(
    val ignoreExclusions: Boolean,
    val ignoreRestrictions: Boolean,
)

enum class PhoneTypes {
    TELEPHONE,
    MOBILE
}
