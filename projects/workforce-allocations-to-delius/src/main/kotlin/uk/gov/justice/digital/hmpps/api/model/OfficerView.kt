package uk.gov.justice.digital.hmpps.api.model

import io.swagger.v3.oas.annotations.media.Schema

data class OfficerView(
    val code: String,
    val name: Name,
    val grade: String?,
    val email: String?,
    @field:Schema(description = "The number of sentenced events with an end date in the next four weeks on cases managed by the officer.")
    val casesDueToEndInNext4Weeks: Long,
    @field:Schema(description = "The number of sentenced custodial events with an \"Expected Release Date\" key date in the next four weeks on cases managed by the officer.")
    val releasesWithinNext4Weeks: Long,
    @field:Schema(description = "The number of parole reports required in the next four weeks on cases managed by the officer.")
    val paroleReportsToCompleteInNext4Weeks: Long
)
