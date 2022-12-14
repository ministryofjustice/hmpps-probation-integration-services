package uk.gov.justice.digital.hmpps.integrations.approvedpremises

import java.time.ZonedDateTime

data class EventDetails<T>(
    val id: String,
    val timestamp: ZonedDateTime,
    val eventType: String,
    val eventDetails: T
)

data class ApplicationSubmitted(
    val applicationId: String,
    val applicationUrl: String,
    val deliusEventNumber: String,
    val targetLocation: String,
    val probationArea: ProbationArea,
    val submittedAt: ZonedDateTime,
    val submittedBy: StaffMember,
)

enum class Decision { Accepted, Rejected }

data class ApplicationAssessed(
    val applicationId: String,
    val applicationUrl: String,
    val deliusEventNumber: String,
    val assessedAt: ZonedDateTime,
    val assessedBy: StaffMember,
    val assessmentArea: ProbationArea,
    val decision: Decision,
    val decisionRationale: String,
)
