package uk.gov.justice.digital.hmpps.integrations.approvedpremises

import com.fasterxml.jackson.annotation.JsonAlias
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReleaseType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.SentenceType
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

const val SERVICE_URN_BASE = "urn:uk:gov:hmpps:approved-premises-service"

interface ContactReferenceable {
    fun externalReference(): String
}

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
) : ContactReferenceable {
    val notes = """
        |An application for a placement in an Approved Premises has been made. The application will be assessed for suitability.
        |Details of the application can be found here: $applicationUrl
    """.trimMargin()

    override fun externalReference() = "$SERVICE_URN_BASE:application-submitted:$applicationId"
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
    val assessmentId: String,
    val assessedAt: ZonedDateTime,
    val assessedBy: AssessedBy,
    val decision: Decision,
    val decisionRationale: String?,
    @JsonAlias("deliusEventNumber")
    val eventNumber: String,
    val arrivalDate: LocalDate?
) : ContactReferenceable {
    val notes: String
        get() = when (decision) {
            Decision.ACCEPTED -> """
                |Application for a placement in an Approved Premises has been assessed as suitable. The application will now be matched to a suitable Approved Premises${additionalNotes()}.
                |Details of the application can be found here: $applicationUrl
            """.trimMargin()

            Decision.REJECTED -> """
                |The application for a placement in an Approved Premises has been assessed for suitability and has been rejected.
                |$decisionRationale
                |Details of the application can be found here: $applicationUrl
            """.trimMargin()
        }

    private fun additionalNotes() =
        arrivalDate?.let { "" } ?: " once a placement request has been received and approved"

    override fun externalReference() = "$SERVICE_URN_BASE:application-assessed:$assessmentId"
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
) : ContactReferenceable {
    override fun externalReference() = "$SERVICE_URN_BASE:application-withdrawn:$applicationId"
}

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
    val applicationSubmittedOn: ZonedDateTime?,
    @JsonAlias("sentenceType")
    private val sentenceTypeString: String?,
    @JsonAlias("releaseType")
    private val releaseTypeString: String?,
    @JsonAlias("situation")
    private val situationString: String?
) : ContactReferenceable {
    val bookingMadeAt: ZonedDateTime = createdAt.truncatedTo(ChronoUnit.SECONDS)
    val sentenceType: SentenceType = SentenceType.from(sentenceTypeString)
    val releaseType: ReleaseType = ReleaseType.from(situationString ?: releaseTypeString)

    override fun externalReference() = "$SERVICE_URN_BASE:booking-made:$bookingId"
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
) : ContactReferenceable {
    var domainEventId: String? = null

    override fun externalReference(): String {
        requireNotNull(domainEventId) { "domainEventId must be set before calling externalReference()" }
        return "$SERVICE_URN_BASE:booking-changed:$domainEventId:$bookingId"
    }
}

data class BookingCancelled(
    val bookingId: String,
    val applicationId: String,
    val applicationUrl: String,
    @JsonAlias("deliusEventNumber")
    val eventNumber: String,
    val premises: Premises,
    val cancelledBy: StaffMember,
    val cancelledAtDate: LocalDate,
    val cancellationRecordedAt: ZonedDateTime,
    val cancellationReason: String
) : ContactReferenceable {
    override fun externalReference() = "$SERVICE_URN_BASE:booking-cancelled:$bookingId"
}

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
) : ContactReferenceable {
    override fun externalReference() = "$SERVICE_URN_BASE:person-not-arrived:$bookingId"
}

data class PersonArrived(
    val bookingId: String,
    val applicationId: String,
    val applicationUrl: String,
    val premises: Premises,
    val recordedBy: StaffMember,
    val applicationSubmittedOn: LocalDate,
    val arrivedAt: ZonedDateTime,
    val expectedDepartureOn: LocalDate?,
    val notes: String?,
    @JsonAlias("deliusEventNumber")
    val eventNumber: String
) : ContactReferenceable {
    override fun externalReference() = "$SERVICE_URN_BASE:person-arrived:$bookingId"
}

data class PersonDeparted(
    val applicationId: String,
    val applicationUrl: String,
    val bookingId: String,
    val recordedBy: StaffMember,
    val departedAt: ZonedDateTime,
    val premises: Premises,
    val legacyReasonCode: String,
    val destination: Destination,
    @JsonAlias("deliusEventNumber")
    val eventNumber: String
) : ContactReferenceable {
    override fun externalReference() = "$SERVICE_URN_BASE:person-departed:$bookingId"
}

data class Destination(val moveOnCategory: MoveOnCategory)
data class MoveOnCategory(@JsonAlias("legacyMoveOnCategoryCode") val legacyCode: String)
