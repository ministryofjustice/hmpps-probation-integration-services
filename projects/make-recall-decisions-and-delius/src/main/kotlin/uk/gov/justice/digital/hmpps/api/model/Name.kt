package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Person
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Staff

data class Name(
    val forename: String,
    val middleName: String?,
    val surname: String,
)

fun Person.name() = Name(forename, listOfNotNull(secondName, thirdName).joinToString(" "), surname)

fun Staff.name() = Name(forename, middleName, surname)
