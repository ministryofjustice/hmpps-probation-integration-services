package uk.gov.justice.digital.hmpps.controller.personaldetails.model

import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CasePersonalContactEntity
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.PersonalContactEntity
import uk.gov.justice.digital.hmpps.integrations.common.model.Name
import uk.gov.justice.digital.hmpps.integrations.common.model.PersonalCircumstance
import uk.gov.justice.digital.hmpps.integrations.common.model.PersonalContact

data class PersonalDetails(
    val crn: String,
    val personalCircumstances: List<PersonalCircumstance>,
    val personalContacts: List<PersonalContact>,
)

fun PersonalContactEntity.name() = Name(forename, middleName, surname)

fun CasePersonalContactEntity.name() = Name(forename, middleName, surname)
