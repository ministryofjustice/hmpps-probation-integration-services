package uk.gov.justice.digital.hmpps.api.model.personalDetails

import jakarta.validation.constraints.Email
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.overview.Disability
import uk.gov.justice.digital.hmpps.api.model.overview.PersonalCircumstance
import uk.gov.justice.digital.hmpps.api.model.overview.Provision
import java.time.LocalDate
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

data class PersonalDetails(
    val crn : String,
    val name: Name,
    val contacts: List<PersonalContact>,
    val mainAddress: Address?,
    val otherAddresses: List<Address>,
    val preferredGender: String,
    val dateOfBirth: LocalDate,
    val preferredName: String?,
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