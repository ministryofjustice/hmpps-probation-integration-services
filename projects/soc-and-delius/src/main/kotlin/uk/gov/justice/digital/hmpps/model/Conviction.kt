package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class Conviction(
    val convictionId: Long,
    val convictionDate: LocalDate?,
    val referralDate: LocalDate,
    val outcome: String,
    val latestCourtAppearanceOutcome: String,
    val offences: List<Offence>,
    val sentence: Sentence?,
    val active: Boolean
)

data class Offence(
    val offenceId: Long,
    val description: String,
    val mainCategoryDescription: String?,
    val mainOffence: Boolean = false
)

data class ConvictionsContainer(
    val convictions: List<Conviction> = listOf()
)

data class Sentence(
    val sentenceId: Long,
    val startDate: LocalDate,
    val expectedEndDate: LocalDate?,
    val custody: Custody?
)

data class Custody(
    val bookingNumber: String?,
    val status: CustodyStatus,
    val keyDates: List<KeyDate>
)

data class CustodyStatus(
    val code: String,
    val description: String
)
