package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class ProbationRecord(
    val crn: String,
    val name: Name,
    val event: Event,
    val activeEvents: List<PrEvent>,
    val inactiveEvents: List<PrEvent>,
)

data class PrEvent(
    val sentence: PrSentence,
    val offences: List<PrOffence>,
    val manager: StaffMember? = null,
)

data class PrSentence(
    val description: String,
    val length: String,
    val startDate: LocalDate,
    val terminationDate: LocalDate?,
)

data class PrOffence(
    val description: String,
    val main: Boolean = false,
)
