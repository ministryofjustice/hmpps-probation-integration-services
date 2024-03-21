package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person

data class Name(
    val forename: String,
    val middleName: String? = null,
    val surname: String
)

fun Person.name() = Name(forename, listOfNotNull(secondName, thirdName).joinToString(" "), surname)
