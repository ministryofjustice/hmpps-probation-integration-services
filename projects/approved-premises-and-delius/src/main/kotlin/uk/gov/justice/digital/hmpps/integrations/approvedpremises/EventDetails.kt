package uk.gov.justice.digital.hmpps.integrations.approvedpremises

import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
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
    val targetLocation: String,
    val submittedAt: ZonedDateTime,
    val submittedBy: SubmittedBy
)

data class SubmittedBy(
    val staffMember: StaffMember,
    val probationArea: ProbationArea
)

enum class Decision { Accepted, Rejected }

data class ApplicationAssessed(
    val applicationId: String,
    val applicationUrl: String,
    val assessedAt: ZonedDateTime,
    val assessedBy: AssessedBy,
    val decision: Decision,
    val decisionRationale: String,
)

data class AssessedBy(
    val staffMember: StaffMember,
    val probationArea: ProbationArea
)

data class BookingMade(
    val bookingId: String,
    val applicationId: String,
    val applicationUrl: String,
    val createdAt: ZonedDateTime,
    val bookedBy: BookedBy,
    val premises: Premises,
)

data class BookedBy(
    val staffMember: StaffMember
)

data class PersonNotArrived(
    val bookingId: String,
    val applicationId: String,
    val applicationUrl: String,
    val recordedBy: StaffMember,
    val premises: Premises,
    val notes: String,
)
