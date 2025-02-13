package uk.gov.justice.digital.hmpps.api.model.personalDetails

import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.sentence.Contact
import java.time.LocalDate

data class PersonalDetails(
    val crn: String,
    val name: Name,
    val contacts: List<PersonalContact>,
    val mainAddress: Address?,
    val otherAddressCount: Int,
    val previousAddressCount: Int,
    val preferredGender: String,
    val dateOfBirth: LocalDate,
    val preferredName: String?,
    val previousSurname: String?,
    val preferredLanguage: String?,
    val genderIdentity: String?,
    val selfDescribedGender: String?,
    val aliases: List<Name>,
    val telephoneNumber: String?,
    val mobileNumber: String?,
    val email: String?,
    val circumstances: Circumstances,
    val disabilities: Disabilities,
    val provisions: Provisions,
    val pnc: String?,
    val noms: String?,
    val sex: String,
    val religionOrBelief: String?,
    val sexualOrientation: String?,
    val requiresInterpreter: Boolean? = false,
    val documents: List<Document>,
    val lastUpdated: LocalDate? = null,
    val lastUpdatedBy: Name? = null,
    val addressTypes: List<AddressType> = emptyList(),
    val staffContacts: List<Contact>
)

data class PersonalDetailsMainAddress(
    val crn: String,
    val name: Name,
    val contacts: List<PersonalContact>,
    val mainAddress: Address? = null,
    val otherAddressCount: Int,
    val previousAddressCount: Int,
    val telephoneNumber: String?,
    val mobileNumber: String?,
    val email: String?,
)

data class AddressType(
    val code: String,
    val description: String
)