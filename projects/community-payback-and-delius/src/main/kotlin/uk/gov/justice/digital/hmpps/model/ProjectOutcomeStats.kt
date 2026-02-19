package uk.gov.justice.digital.hmpps.model

data class ProjectOutcomeStats(
    val project: Project,
    val overdueOutcomesCount: Int,
    val oldestOverdueInDays: Int,
)