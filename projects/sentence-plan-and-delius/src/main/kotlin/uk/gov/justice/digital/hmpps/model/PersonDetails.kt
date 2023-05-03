package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

class PersonDetails(
    val name: Name,
    val crn: String,
    val tier: String?,
    val dateOfBirth: LocalDate,
    val nomisId: String?,
    val region: String?,
    val keyWorker: Manager
)

data class Name(
    val forename: String,
    val middleName: String?,
    val surname: String
)

data class Manager(
    val name: Name?,
    val unallocated: Boolean = false
)
