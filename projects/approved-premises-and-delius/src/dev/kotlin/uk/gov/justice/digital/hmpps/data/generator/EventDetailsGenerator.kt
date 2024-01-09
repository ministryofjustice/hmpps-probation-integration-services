package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.approvedpremises.*
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReleaseType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.SentenceType
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

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
            submittedBy = submittedBy,
            "3"
        )
    )

    fun applicationAssessed(assessedBy: AssessedBy, arrivalDate: LocalDate? = null) = EventDetails(
        id = UUID.randomUUID().toString(),
        timestamp = ZonedDateTime.now(),
        eventType = "approved-premises.application.assessed",
        eventDetails = ApplicationAssessed(
            applicationId = UUID.randomUUID().toString(),
            applicationUrl = "https://example.com",
            assessedAt = ZonedDateTime.now(),
            assessedBy = assessedBy,
            decision = Decision.ACCEPTED,
            decisionRationale = "Test decision rationale",
            "7",
            arrivalDate
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
            premises = premises(),
            createdAt = ZonedDateTime.now(),
            bookedBy = bookedBy,
            eventNumber = "23",
            arrivalOn = LocalDate.now(),
            departureOn = LocalDate.now(),
            applicationSubmittedOn = ZonedDateTime.now().minusDays(2),
            sentenceTypeString = SentenceType.StandardDeterminate.value,
            releaseTypeString = ReleaseType.Licence.value,
            situationString = null
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
            premises = premises(),
            recordedBy = staffMember(recordedBy),
            notes = "TEST",
            reason = "The reason they didn't attend",
            reasonCode = "A",
            eventNumber = "17"
        )
    )

    fun personArrived(keyWorker: Staff) = EventDetails(
        id = UUID.randomUUID().toString(),
        timestamp = ZonedDateTime.now(),
        eventType = "approved-premises.person.arrived",
        eventDetails = PersonArrived(
            bookingId = UUID.randomUUID().toString(),
            applicationId = UUID.randomUUID().toString(),
            applicationUrl = "https://example.com",
            premises = premises(),
            arrivedAt = ZonedDateTime.now(),
            expectedDepartureOn = LocalDate.now().plusMonths(6),
            keyWorker = staffMember(keyWorker),
            notes = "Arrived on time",
            applicationSubmittedOn = LocalDate.now().minusDays(1),
            eventNumber = "11"
        )
    )

    private fun staffMember(staff: Staff, username: String? = null) = StaffMember(
        username = username,
        staffCode = staff.code,
        staffIdentifier = staff.id,
        forenames = staff.forename,
        surname = staff.surname
    )

    private fun premises() = Premises(
        id = UUID.randomUUID().toString(),
        name = "Test Premises",
        apCode = "TEST",
        legacyApCode = ApprovedPremisesGenerator.DEFAULT.code.code
    )
}
