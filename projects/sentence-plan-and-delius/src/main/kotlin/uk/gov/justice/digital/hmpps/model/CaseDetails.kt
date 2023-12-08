package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class CaseDetails(
    val name: Name,
    val crn: String,
    val tier: String?,
    val dateOfBirth: LocalDate,
    val nomisId: String?,
    val region: String?,
    val keyWorker: Manager,
    val inCustody: Boolean = false,
)

data class Name(
    val forename: String,
    val middleName: String?,
    val surname: String,
)

data class Manager(
    val name: Name?,
    val unallocated: Boolean = false,
)
