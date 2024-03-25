package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class RiskRecord(
    val crn: String,
    val name: Name,
    val activeRegistrations: List<RiskRegistration>,
    val inactiveRegistrations: List<RiskRegistration>,
    val ogrs: RiskOGRS? = null
)

data class RiskRegistration(
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val notes: String? = null,
    val flag: RegistrationFlag? = null
)

data class RegistrationFlag(val description: String)

data class RiskOGRS(
    val lastUpdatedDate: LocalDate,
    val score: Long
)
