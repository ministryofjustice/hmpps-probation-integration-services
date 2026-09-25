package uk.gov.justice.digital.hmpps.api.model.overview

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.api.model.Name
import java.time.LocalDate

@Schema(name = "OverviewPersonalDetails")
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
    val allowSms: Boolean?,
)