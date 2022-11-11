package uk.gov.justice.digital.hmpps.integrations.oasys.model

import java.time.ZonedDateTime

data class OasysTimelineAssessment(
    val assessmentPk: Long,
    val assessmentType: String,
    val initiationDate: ZonedDateTime,
    val status: String,
    val completedDate: ZonedDateTime?,
)
