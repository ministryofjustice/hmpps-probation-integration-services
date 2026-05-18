package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.LocalDate
import java.time.LocalDateTime

fun ReferenceData.keyValueOf() = KeyValue(code, description)

data class OffenderDetail(
    val preferredName: String?,
    val contactDetails: OffenderContactDetails,
    val dateOfBirth: LocalDate,
    val firstName: String,
    val middleNames: List<String> = emptyList(),
    val offenderProfile: OffenderProfile,
    val offenderAliases: List<OffenderAlias> = emptyList(),
    val otherIds: OtherIds,
    val currentTier: String?,
    val previousSurname: String?,
    val surname: String,
    val title: String?,
    val currentExclusion: Boolean,
    val currentRestriction: Boolean,
)

data class OffenderAlias(
    val id: String,
    val dateOfBirth: LocalDate?,
    val firstName: String,
    val middleNames: List<String> = emptyList(),
    val surname: String,
    val gender: String
)

data class OffenderContactDetails(
    val allowSMS: Boolean?,
    val emailAddresses: List<String> = emptyList(),
    val phoneNumbers: List<PhoneNumber> = emptyList(),
    val addresses: List<OffenderAddress> = emptyList()
)

data class KeyValue(
    val code: String? = null,
    val description: String
)

data class Provision(
    val category: KeyValue?,
    val finishDate: LocalDate?,
    val notes: String?,
    val provisionId: Long,
    val provisionType: KeyValue,
    val startDate: LocalDate,
    val lastUpdatedDate: LocalDate?
)

data class OffenderAddress(
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val noFixedAbode: Boolean? = null,
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
    val disabilityCondition: KeyValue?,
    val disabilityId: Long,
    val disabilityType: KeyValue,
    val endDate: LocalDate?,
    val isActive: Boolean,
    val notes: String?,
    val startDate: LocalDate
)

data class OffenderLanguages(
    val languageConcerns: String?,
    val primaryLanguage: String?,
    val requiresInterpreter: Boolean?
)

data class OffenderProfile(
    val gender: String,
    val genderIdentity: String?,
    val selfDescribedGender: String?,
    val disabilities: List<Disability> = emptyList(),
    val ethnicity: String?,
    val immigrationStatus: String?,
    val nationality: String?,
    val offenderLanguages: OffenderLanguages,
    val provisions: List<Provision> = emptyList(),
    val religion: String?,
    val secondaryNationality: String?,
    val sexualOrientation: String?
)

data class PhoneNumber(
    val number: String?,
    val type: String
)

enum class PhoneTypes {
    TELEPHONE,
    MOBILE
}
