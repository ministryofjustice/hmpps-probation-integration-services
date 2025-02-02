package uk.gov.justice.digital.hmpps.model

import java.time.ZonedDateTime

data class CourtAppearance(
    val appearanceDate: ZonedDateTime,
    val type: Type,
    val courtCode: String,
    val courtName: String,
    val crn: String,
    val courtAppearanceId: Long,
    val offenderId: Long
)

data class Type(
    val code: String,
    val description: String
)

data class CourtAppearancesContainer(
    val courtAppearances: List<CourtAppearance> = listOf()
)

data class AllCourtAppearancesContainer(
    val courtAppearances: Map<String, List<CourtAppearance>> = mapOf()
)
