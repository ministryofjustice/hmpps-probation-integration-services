package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.DeliusDateFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.OGRSAssessment
import uk.gov.justice.digital.hmpps.integrations.delius.entity.OGRSAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.OGRS_ASSESSMENT_CT
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.entity.getManager
import uk.gov.justice.digital.hmpps.messaging.OgrsScore
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@Service
class RiskAssessmentService(
    private val eventRepository: EventRepository,
    private val personRepository: PersonRepository,
    private val ogrsAssessmentRepository: OGRSAssessmentRepository,
    private val telemetryService: TelemetryService,
    private val personManagerRepository: PersonManagerRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository
) {

    fun addOrUpdateRiskAssessment(
        crn: String,
        eventNumber: String?,
        assessmentDate: ZonedDateTime,
        ogrsScore: OgrsScore
    ) {
        // validate that the CRN is for a real offender
        val person = personRepository.findByCrn(crn) ?: return let {
            telemetryService.trackEvent(
                "PersonNotFound",
                mapOf("crn" to crn)
            )
        }

        // validate that the event number is present
        if (eventNumber == null) {
            telemetryService.trackEvent(
                "Event number not present",
                mapOf("crn" to crn)
            )
            return
        }

        // validate that the offender has an event with this event number
        val event = eventRepository.findByCrn(crn, eventNumber) ?: return let {
            telemetryService.trackEvent(
                "event not found",
                mapOf("crn" to crn, "eventNumber" to eventNumber)
            )
        }

        val ogrsAssessment = ogrsAssessmentRepository.findByEvent(event)
        if (ogrsAssessment != null) {
            if (assessmentDate.toLocalDate() > ogrsAssessment.assessmentDate) {
                // if there is one and this assessment has a greater date then update the existing with the new scores
                val assessment = ogrsAssessmentRepository.save(
                    ogrsAssessment.copy(
                        ogrs3Score1 = ogrsScore.ogrs3Yr1.toLong(),
                        ogrs3Score2 = ogrsScore.ogrs3Yr2.toLong(),
                        assessmentDate = assessmentDate.toLocalDate()
                    )
                )
                createContact(assessment, person, event, assessmentDate, ogrsScore)
            }
        } else {
            // if there is no OGRS_ASSESSMENT for this crn/event then create a new one
            val assessment = ogrsAssessmentRepository.save(
                OGRSAssessment(
                    0,
                    assessmentDate.toLocalDate(),
                    event,
                    ogrsScore.ogrs3Yr1.toLong(),
                    ogrsScore.ogrs3Yr2.toLong()
                )
            )
            createContact(assessment, person, event, assessmentDate, ogrsScore)
        }
    }

    private fun createContact(
        assessment: OGRSAssessment,
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
                notes = generateNotes(person, assessmentDate, ogrsScore, personManager, event),
                staffId = personManager.staff.id,
                teamId = personManager.team.id
            )
        )
    }

    private fun generateNotes(
        person: Person,
        assessmentDate: ZonedDateTime,
        ogrsScore: OgrsScore,
        personManager: PersonManager,
        event: Event
    ): String {
        return """
            CRN: ${person.crn}
            PNC Number: ${person.pncNumber}
            Name: ${person.forename} ${person.surname}
            Order: ${event.disposal?.disposalType?.description}
            Offender manager: ${personManager.staff.forename} ${personManager.staff.surname}
            Gender: ${person.gender?.description}
            Date of Birth: ${DeliusDateFormatter.format(person.dateOfBirth)}
            Date of Current Conviction: ${DeliusDateFormatter.format(event.disposal?.disposalDate)}
            Date of Assessment: ${DeliusDateFormatter.format(assessmentDate)}
            Date of First Sanction: TODO()
            Previous Sanctions: TODO()
            Offence Category: ${event.mainOffence?.offence?.mainCategoryDescription}
            Reconviction calculation is ${ogrsScore.ogrs3Yr1}% within one year and ${ogrsScore.ogrs3Yr2}% within 2 years.
        """.trimIndent()
    }
}
