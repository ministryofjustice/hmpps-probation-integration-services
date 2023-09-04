package uk.gov.justice.digital.hmpps.integrations.oasys

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.ZonedDateTime

data class OasysTimelineResponse(
    val crn: String,
    @JsonAlias("limitedAccessOffender") val limitedAccess: Boolean,
    val timeline: List<Timeline>
)

data class Timeline(
    @JsonAlias("assessmentPk") val id: Int,
    @JsonAlias("assessmentType") val type: String,
    @JsonAlias("initiationDate") val initiationDate: ZonedDateTime,
    @JsonAlias("status") val status: String,
    @JsonAlias("completedDate") val completedDate: ZonedDateTime?
)
