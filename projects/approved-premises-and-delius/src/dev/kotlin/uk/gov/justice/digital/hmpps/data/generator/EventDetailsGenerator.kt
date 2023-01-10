package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ApplicationAssessed
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ApplicationSubmitted
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.AssessedBy
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.BookedBy
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.BookingMade
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.Decision
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.EventDetails
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonNotArrived
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.Premises
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.StaffMember
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.SubmittedBy
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import java.time.ZonedDateTime
import java.util.UUID

object EventDetailsGenerator {
    fun applicationSubmitted(submittedBy: SubmittedBy) = EventDetails(
        id = UUID.randomUUID().toString(),
        timestamp = ZonedDateTime.now(),
        eventType = "approved-premises.application.submitted",
        eventDetails = ApplicationSubmitted(
            applicationId = UUID.randomUUID().toString(),
            applicationUrl = "https://example.com",
            targetLocation = "TEST",
            submittedAt = ZonedDateTime.now(),
            submittedBy = submittedBy
        )
    )

    fun applicationAssessed(assessedBy: AssessedBy) = EventDetails(
        id = UUID.randomUUID().toString(),
        timestamp = ZonedDateTime.now(),
        eventType = "approved-premises.application.assessed",
        eventDetails = ApplicationAssessed(
            applicationId = UUID.randomUUID().toString(),
            applicationUrl = "https://example.com",
            assessedAt = ZonedDateTime.now(),
            assessedBy = assessedBy,
            decision = Decision.Accepted,
            decisionRationale = "Test decision rationale"
        )
    )

    fun bookingMade(bookedBy: BookedBy) = EventDetails(
        id = UUID.randomUUID().toString(),
        timestamp = ZonedDateTime.now(),
        eventType = "approved-premises.booking.made",
        eventDetails = BookingMade(
            bookingId = UUID.randomUUID().toString(),
            applicationId = UUID.randomUUID().toString(),
            applicationUrl = "https://example.com",
            premises = Premises(
                id = UUID.randomUUID().toString(),
                name = "Test Premises",
                apCode = "TEST",
                legacyApCode = "TEST",
                probationArea = probationArea()
            ),
            createdAt = ZonedDateTime.now(),
            bookedBy = bookedBy
        )
    )

    fun personNotArrived(recordedBy: Staff) = EventDetails(
        id = UUID.randomUUID().toString(),
        timestamp = ZonedDateTime.now(),
        eventType = "approved-premises.person.not-arrived",
        eventDetails = PersonNotArrived(
            bookingId = UUID.randomUUID().toString(),
            applicationId = UUID.randomUUID().toString(),
            applicationUrl = "https://example.com",
            premises = Premises(
                id = UUID.randomUUID().toString(),
                name = "Test Premises",
                apCode = "TEST",
                legacyApCode = "TEST",
                probationArea = probationArea()
            ),
            recordedBy = staffMember(recordedBy),
            notes = "TEST"
        )
    )

    private fun probationArea() = ProbationArea(
        code = ProbationAreaGenerator.DEFAULT.code,
        name = "TEST"
    )

    private fun staffMember(staff: Staff) = StaffMember(
        username = "TEST",
        staffCode = staff.code,
        staffIdentifier = staff.id,
        forenames = staff.forename,
        surname = staff.surname,
    )
}
