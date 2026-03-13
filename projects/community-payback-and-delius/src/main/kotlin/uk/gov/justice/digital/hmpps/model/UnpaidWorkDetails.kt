package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class UnpaidWorkDetails(
    val case: Case,
    val unpaidWorkDetails: List<UnpaidWorkMinutes>
)

data class UnpaidWorkMinutes(
    val eventNumber: Long,
    val sentenceDate: LocalDate,
    val requiredMinutes: Long,
    val adjustments: Long,
    val completedMinutes: Long,
    val completedEteMinutes: Long,
    val eventOutcome: String,
    val upwStatus: String?,
    val referralDate: LocalDate,
    val convictionDate: LocalDate,
    val court: CodeDescription,
    val mainOffence: Offence
)

data class Offence(
    val date: LocalDate,
    val count: Int,
    val code: String,
    val description: String
)