package uk.gov.justice.digital.hmpps.api.model.overview

import uk.gov.justice.digital.hmpps.api.model.Name
import java.time.LocalDate

data class OverviewPersonalDetails(
    val name: Name,
    val preferredGender: String,
    val dateOfBirth: LocalDate,
    val preferredName: String?,
    val telephoneNumber: String?,
    val mobileNumber: String?,
    val disabilities: List<OverviewDisability>,
    val provisions: List<OverviewProvision>,
    val personalCircumstances: List<PersonalCircumstance>,
    val allowSms: Boolean?,
)
