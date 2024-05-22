package uk.gov.justice.digital.hmpps.api.model

import uk.gov.justice.digital.hmpps.entity.ProbationArea
import uk.gov.justice.digital.hmpps.entity.Staff
import uk.gov.justice.digital.hmpps.entity.Team

data class AuthorDetails(
    val staff: Staff,
    val team: Team,
    val probationArea: ProbationArea,
)
