package uk.gov.justice.digital.hmpps.integrations.oasys

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysAssessment(
    val assessmentPk: Long,
    val assessmentType: String,
    val initiationDate: ZonedDateTime,
    val status: String,
    val completedDate: ZonedDateTime?,
)
