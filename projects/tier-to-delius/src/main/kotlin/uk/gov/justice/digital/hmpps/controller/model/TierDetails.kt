package uk.gov.justice.digital.hmpps.controller.model

import java.time.LocalDate

data class TierDetails(
    val gender: String,
    val currentTier: String?,
    val oGRSScore: Long?,
    val rSRScore: Double?,
    val registrations: List<Registration>,
    val convictions: List<Conviction>,
    val previousEnforcementActivity: Boolean,
)

data class Registration(
    val code: String,
    val description: String,
    val level: String?,
    val date: LocalDate,
)

data class Conviction(
    val terminationDate: LocalDate?,
    val sentenceTypeCode: String?,
    val breached: Boolean,
    val requirements: List<Requirement>,
)

data class Requirement(
    val mainCategoryTypeCode: String,
    val restrictive: Boolean,
)
