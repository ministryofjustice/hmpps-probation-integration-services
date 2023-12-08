package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.assessment.entity.OasysAssessment
import uk.gov.justice.digital.hmpps.integrations.delius.assessment.entity.OasysAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.assessment.entity.SentencePlan
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getByNumber
import uk.gov.justice.digital.hmpps.integrations.oasys.AssessmentSummary
import java.time.LocalDate

@Service
class AssessmentService(
    private val oasysAssessmentRepository: OasysAssessmentRepository,
    private val eventRepository: EventRepository,
    private val contactService: ContactService
) {
    fun recordAssessment(person: Person, summary: AssessmentSummary) {
        val previousAssessment = oasysAssessmentRepository.findByOasysId(summary.oasysId)

        val event = eventRepository.getByNumber(person.id, summary.eventNumber)
        val contact = previousAssessment?.contact?.withDateTeamAndStaff(
            LocalDate.now(),
            person.manager.teamId,
            person.manager.staffId
        ) ?: contactService.createContact(
            summary.contactDetail(),
            person,
            event
        )

        previousAssessment?.also(oasysAssessmentRepository::delete)
        oasysAssessmentRepository.save(summary.oasysAssessment(contact))
    }
}

private fun AssessmentSummary.contactDetail() =
    ContactDetail(ContactType.Code.OASYS_ASSESSMENT, LocalDate.now(), "Reason for Assessment: $description")

fun AssessmentSummary.oasysAssessment(contact: Contact): OasysAssessment {
    TODO()
}

fun AssessmentSummary.sentencePlan(): SentencePlan {
    TODO()
}
