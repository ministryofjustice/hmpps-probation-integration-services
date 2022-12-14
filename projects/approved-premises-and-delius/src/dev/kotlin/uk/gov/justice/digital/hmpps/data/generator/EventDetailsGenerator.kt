package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ApplicationAssessed
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ApplicationSubmitted
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.Decision
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.EventDetails
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.StaffMember
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import java.time.ZonedDateTime
import java.util.UUID

object EventDetailsGenerator {
    fun applicationSubmitted(submittedBy: Staff) = EventDetails(
        id = UUID.randomUUID().toString(),
        timestamp = ZonedDateTime.now(),
        eventType = "approved-premises.application.submitted",
        eventDetails = ApplicationSubmitted(
            applicationId = UUID.randomUUID().toString(),
            applicationUrl = "http://example.com",
            deliusEventNumber = "123",
            targetLocation = "TEST",
            probationArea = ProbationArea(
                code = ProbationAreaGenerator.DEFAULT.code,
                name = "TEST"
            ),
            submittedAt = ZonedDateTime.now(),
            submittedBy = StaffMember(
                username = "TEST",
                staffCode = submittedBy.code,
                staffIdentifier = submittedBy.id,
                forenames = submittedBy.forename,
                surname = submittedBy.surname,
            ),
        )
    )

    fun applicationAssessed(assessedBy: Staff) = EventDetails(
        id = UUID.randomUUID().toString(),
        timestamp = ZonedDateTime.now(),
        eventType = "approved-premises.application.assessed",
        eventDetails = ApplicationAssessed(
            applicationId = UUID.randomUUID().toString(),
            applicationUrl = "http://example.com",
            deliusEventNumber = "123",
            assessmentArea = ProbationArea(
                code = ProbationAreaGenerator.DEFAULT.code,
                name = "TEST"
            ),
            assessedAt = ZonedDateTime.now(),
            assessedBy = StaffMember(
                username = "TEST",
                staffCode = assessedBy.code,
                staffIdentifier = assessedBy.id,
                forenames = assessedBy.forename,
                surname = assessedBy.surname,
            ),
            decision = Decision.Accepted,
            decisionRationale = "Test decision rationale"
        )
    )
}
