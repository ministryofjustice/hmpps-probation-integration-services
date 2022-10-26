package uk.gov.justice.digital.hmpps.integrations.oasys

import java.time.ZonedDateTime

data class OasysAssessment(
    val assessmentPk: Long,
    val assessmentType: String,
    val initiationDate: ZonedDateTime,
    val status: String,
    val completedDate: ZonedDateTime?,
)
