package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.messaging.OgrsScore
import java.time.ZonedDateTime

@Service
class RiskAssessmentService(
    private val eventRepository: EventRepository,
    private val personRepository: PersonRepository,
    private val ogrsAssessmentRepository: OGRSAssessmentRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
    private val additionalIdentifierRepository: AdditionalIdentifierRepository,
) {

    @Transactional
    fun addOrUpdateRiskAssessment(
        crn: String,
        eventNumber: Int?,
        assessmentDate: ZonedDateTime,
        ogrsScore: OgrsScore
    ) {
        // validate that the CRN is for a real offender
        val person = getOgrsPerson(crn)

        // if crn is different it's a merged record so use null to get latest event
        val personEventNumber = if (person.crn == crn) eventNumber else null
        val event = personEventNumber?.let { eventRepository.getByCrn(crn, it.toString()) }
            ?: eventRepository.findMostRecent(person.id)
            ?: throw DeliusValidationError("Event Number = Null and no active events for the case")

        if (!event.active) {
            throw DeliusValidationError("Event is Terminated")
        }

        val ogrsAssessment = ogrsAssessmentRepository.findByEvent(event)
        if (ogrsAssessment != null) {
            if (assessmentDate.toLocalDate() > ogrsAssessment.assessmentDate) {
                // if there is one and this assessment has a greater date then update the existing with the new scores
                ogrsAssessment.ogrs3Score1 = ogrsScore.ogrs3Yr1.toLong()
                ogrsAssessment.ogrs3Score2 = ogrsScore.ogrs3Yr2.toLong()
                ogrsAssessment.assessmentDate = assessmentDate.toLocalDate()
                ogrsAssessmentRepository.save(ogrsAssessment)
                createContact(person, event, assessmentDate, ogrsScore)
            }
        } else {
            // if there is no OGRS_ASSESSMENT for this crn/event then create a new one
            eventRepository.findForUpdate(event.id)
            ogrsAssessmentRepository.findByEvent(event)?.let { throw ConflictException("Assessment has been created") }
            ogrsAssessmentRepository.save(
                OGRSAssessment(
                    0,
                    assessmentDate.toLocalDate(),
                    event,
                    ogrsScore.ogrs3Yr1.toLong(),
                    ogrsScore.ogrs3Yr2.toLong()
                )
            )
            createContact(person, event, assessmentDate, ogrsScore)
        }
    }

    private fun createContact(
        person: Person,
        event: Event,
        assessmentDate: ZonedDateTime,
        ogrsScore: OgrsScore
    ) {
        val personManager = personManagerRepository.getManager(person.id)
        contactRepository.save(
            Contact(
                type = contactTypeRepository.getByCode(OGRS_ASSESSMENT_CT),
                date = assessmentDate,
                event = event,
                person = person,
                notes = generateNotes(person, ogrsScore, event),
                staffId = personManager.staff.id,
                teamId = personManager.team.id
            )
        )
    }

    private fun generateNotes(person: Person, ogrsScore: OgrsScore, event: Event): String {
        return """
            CRN: ${person.crn}
            PNC Number: ${person.pncNumber}
            Name: ${person.forename} ${person.surname}
            Order: ${event.disposal?.disposalType?.description ?: ""}
            Reconviction calculation is ${ogrsScore.ogrs3Yr1}% within one year and ${ogrsScore.ogrs3Yr2}% within 2 years.
        """.trimIndent()
    }

    private fun getOgrsPerson(crn: String): Person {
        val person = personRepository.getByCrn(crn)
        if (!person.softDeleted) return person
        val newCrn = additionalIdentifierRepository.findLatestMergedToCrn(person.id)?.identifier
        return newCrn?.let { personRepository.getByCrn(it) }?.takeIf { !it.softDeleted }
            ?: throw NotFoundException("Person", if (newCrn == null) "crn" else "mergedCrn", newCrn ?: crn)
    }
}
