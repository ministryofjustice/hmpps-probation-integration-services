package uk.gov.justice.digital.hmpps.integrations.approvedpremises

import com.fasterxml.jackson.annotation.JsonAlias
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

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
    val submittedBy: SubmittedBy,
    @JsonAlias("deliusEventNumber")
    val eventNumber: String
) {
    val notes = """
        |An application for a placement in an Approved Premises has been made. The application will be assessed for suitability.
        |Details of the application can be found here: $applicationUrl
    """.trimMargin()
}

data class SubmittedBy(
    val staffMember: StaffMember,
    val probationArea: ProbationArea
)

enum class Decision {
    ACCEPTED, REJECTED;

    override fun toString(): String {
        return when (this) {
            ACCEPTED -> "Accepted"
            REJECTED -> "Rejected"
        }
    }
}

data class ApplicationAssessed(
    val applicationId: String,
    val applicationUrl: String,
    val assessedAt: ZonedDateTime,
    val assessedBy: AssessedBy,
    val decision: Decision,
    val decisionRationale: String?,
    @JsonAlias("deliusEventNumber")
    val eventNumber: String
) {
    val notes: String
        get() = when (decision) {
            Decision.ACCEPTED -> """
                |Application for a placement in an Approved Premises has been assessed as suitable. The application will now be matched to a suitable Approved Premises.
                |Details of the application can be found here: $applicationUrl
            """.trimMargin()

            Decision.REJECTED -> """
                |The application for a placement in an Approved Premises has been assessed for suitability and has been rejected.
                |$decisionRationale
                |Details of the application can be found here: $applicationUrl
            """.trimMargin()
        }
}

data class AssessedBy(
    val staffMember: StaffMember,
    val probationArea: ProbationArea
)

data class ApplicationWithdrawn(
    val applicationId: String,
    val applicationUrl: String,
    @JsonAlias("deliusEventNumber")
    val eventNumber: String,
    val withdrawnAt: ZonedDateTime,
    val withdrawnBy: WithdrawnBy,
    val withdrawalReason: String
)

data class WithdrawnBy(
    val staffMember: StaffMember,
    val probationArea: ProbationArea
)

data class BookingMade(
    val bookingId: String,
    val applicationId: String,
    val applicationUrl: String,
    private val createdAt: ZonedDateTime,
    @JsonAlias("deliusEventNumber")
    val eventNumber: String,
    val bookedBy: BookedBy,
    val premises: Premises,
    val arrivalOn: LocalDate,
    val departureOn: LocalDate,
    val submittedAt: ZonedDateTime?
) {
    val bookingMadeAt: ZonedDateTime = createdAt.truncatedTo(ChronoUnit.SECONDS)
}

data class BookingChanged(
    val bookingId: String,
    val applicationId: String,
    val applicationUrl: String,
    @JsonAlias("deliusEventNumber")
    val eventNumber: String,
    val premises: Premises,
    val changedBy: StaffMember,
    val changedAt: ZonedDateTime,
    val arrivalOn: LocalDate,
    val departureOn: LocalDate
)

data class BookingCancelled(
    val bookingId: String,
    val applicationId: String,
    val applicationUrl: String,
    @JsonAlias("deliusEventNumber")
    val eventNumber: String,
    val premises: Premises,
    val cancelledBy: StaffMember,
    val cancelledAt: ZonedDateTime,
    val cancellationReason: String
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
    val notes: String?,
    val reason: String,
    @JsonAlias("legacyReasonCode")
    val reasonCode: String,
    @JsonAlias("deliusEventNumber")
    val eventNumber: String
)

data class PersonArrived(
    val bookingId: String,
    val applicationId: String,
    val applicationUrl: String,
    val premises: Premises,
    val keyWorker: StaffMember,
    val applicationSubmittedOn: LocalDate,
    val arrivedAt: ZonedDateTime,
    val expectedDepartureOn: LocalDate?,
    val notes: String?,
    @JsonAlias("deliusEventNumber")
    val eventNumber: String
)

data class PersonDeparted(
    val applicationId: String,
    val applicationUrl: String,
    val bookingId: String,
    val keyWorker: StaffMember,
    val departedAt: ZonedDateTime,
    val premises: Premises,
    val legacyReasonCode: String,
    val destination: Destination,
    @JsonAlias("deliusEventNumber")
    val eventNumber: String
)

data class Destination(val moveOnCategory: MoveOnCategory)
data class MoveOnCategory(@JsonAlias("legacyMoveOnCategoryCode") val legacyCode: String)
