package uk.gov.justice.digital.hmpps.api.model.personalDetails

import uk.gov.justice.digital.hmpps.api.model.Name
import java.time.LocalDate

data class PersonalDetails(
    val crn: String,
    val name: Name,
    val contacts: List<PersonalContact>,
    val mainAddress: Address?,
    val otherAddresses: List<Address> = emptyList(),
    val previousAddresses: List<Address> = emptyList(),
    val preferredGender: String,
    val dateOfBirth: LocalDate,
    val preferredName: String?,
    val previousSurname: String?,
    val preferredLanguage: String?,
    val aliases: List<Name>,
    val telephoneNumber: String?,
    val mobileNumber: String?,
    val email: String?,
    val circumstances: Circumstances,
    val disabilities: Disabilities,
    val provisions: Provisions,
    val pnc: String?,
    val sex: String,
    val religionOrBelief: String?,
    val sexualOrientation: String?,
    val documents: List<Document>

)