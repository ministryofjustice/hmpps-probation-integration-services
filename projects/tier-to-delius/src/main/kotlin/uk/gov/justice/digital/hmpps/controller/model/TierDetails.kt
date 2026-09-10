package uk.gov.justice.digital.hmpps.controller.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class TierDetails(
    val gender: String,
    val currentTier: String?,
    @JsonProperty("ogrsscore")
    val ogrsScore: Long?,
    @JsonProperty("rsrscore")
    val rsrScore: Double?,
    val registrations: List<Registration>,
    val convictions: List<Conviction>,
    val previousEnforcementActivity: Boolean,
    val latestReleaseDate: LocalDate?,
    val hasActiveEvent: Boolean
)

data class Registration(
    val code: String,
    val description: String,
    val level: String?,
    val category: String?,
    val date: LocalDate
)

data class Conviction(
    val startDate: LocalDate,
    val terminationDate: LocalDate?,
    val latestReleaseDate: LocalDate?,
    val isCustodial: Boolean,
    val sentenceTypeCode: String?,
    val breached: Boolean,
    val requirements: List<Requirement>,
    val mainOffence: Offence,
    val additionalOffences: List<Offence>,
)

data class Requirement(
    val mainCategoryTypeCode: String,
    val restrictive: Boolean
)

data class Offence(
    val code: String,
    val description: String,
    val sentencingAct2026Exclusion: Boolean,
)
