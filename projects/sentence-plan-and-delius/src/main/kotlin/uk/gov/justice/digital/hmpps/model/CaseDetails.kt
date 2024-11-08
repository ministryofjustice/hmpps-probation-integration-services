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
    val sentences: List<Sentence> = emptyList()
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

data class Sentence(
    val description: String?,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val programmeRequirement: Boolean = false,
    val unpaidWorkHoursOrdered: Int = 0,
    val unpaidWorkMinutesCompleted: Int = 0,
    val rarDaysOrdered: Int = 0,
    val rarDaysCompleted: Int = 0,
)
