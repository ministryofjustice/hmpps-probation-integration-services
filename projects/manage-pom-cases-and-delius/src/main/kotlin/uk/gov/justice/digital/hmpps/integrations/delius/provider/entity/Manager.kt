package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

interface Manager {
    val probationArea: ProbationArea
    val team: Team
    val staff: Staff
}
