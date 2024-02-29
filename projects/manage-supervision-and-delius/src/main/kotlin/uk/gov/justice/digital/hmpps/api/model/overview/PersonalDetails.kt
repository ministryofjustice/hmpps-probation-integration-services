package uk.gov.justice.digital.hmpps.api.model.overview

import uk.gov.justice.digital.hmpps.api.model.Name
import java.time.LocalDate

data class PersonalDetails(
    val name: Name,
    val preferredGender: String,
    val dateOfBirth: LocalDate,
    val preferredName: String?,
    val telephoneNumber: String?,
    val mobileNumber: String?,
    val disabilities: List<Disability>,
    val provisions: List<Provision>,
    val personalCircumstances: List<PersonalCircumstance>,
)