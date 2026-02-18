package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException.Companion.orIgnore
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.messaging.Ogrs4Score
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
        ogrsScore: Ogrs4Score
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
        val arpValues = ogrsScore.arpValues()
        if (ogrsAssessment != null) {
            if (assessmentDate.toLocalDate() > ogrsAssessment.assessmentDate) {
                // if there is one and this assessment has a greater date then update the existing with the new scores
                ogrsAssessment.ogrs3Score1 = ogrsScore.ogrs3Yr1?.toLong()
                ogrsAssessment.ogrs3Score2 = ogrsScore.ogrs3Yr2?.toLong()
                ogrsAssessment.arpStaticDynamic = arpValues.arpStaticDynamic
                ogrsAssessment.arpScore = arpValues.arpScore
                ogrsAssessment.arpBand = arpValues.arpBand
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
                    ogrsScore.ogrs3Yr1?.toLong(),
                    ogrsScore.ogrs3Yr2?.toLong(),
                    arpValues.arpStaticDynamic,
                    arpValues.arpScore,
                    arpValues.arpBand
                )
            )
            createContact(person, event, assessmentDate, ogrsScore)
        }
    }

    private fun createContact(
        person: Person,
        event: Event,
        assessmentDate: ZonedDateTime,
        ogrsScore: Ogrs4Score
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

    private fun generateNotes(person: Person, ogrsScore: Ogrs4Score, event: Event): String {
        val arpValues = ogrsScore.arpValues()
        val arpStaticDynamic = when (arpValues.arpStaticDynamic) {
            "S" -> "Static"
            "D" -> "Dynamic"
            else -> null
        }
        return listOfNotNull(
            "CRN: ${person.crn}",
            "PNC Number: ${person.pncNumber}",
            "Name: ${person.forename} ${person.surname}",
            "Order: ${event.disposal?.disposalType?.description ?: ""}",
            if (ogrsScore.ogrs3Yr1 != null && ogrsScore.ogrs3Yr2 != null) {
                "OGRS3: ${ogrsScore.ogrs3Yr1}% within 1 year and ${ogrsScore.ogrs3Yr2}% within 2 years."
            } else null,
            if (arpValues.arpScore != null && arpValues.arpBand != null && arpValues.arpStaticDynamic != null) {
                "All Reoffending Predictor (ARP): $arpStaticDynamic ARP score is ${arpValues.arpScore}% - ${arpValues.arpBand.fromBand()}"
            } else null
        ).joinToString("\n")
    }

    private fun String?.fromBand() = when (this) {
        "V" -> "Very high"
        "H" -> "High"
        "M" -> "Medium"
        "L" -> "Low"
        else -> this
    }

    private fun getOgrsPerson(crn: String): Person {
        val person = personRepository.getByCrn(crn)
        if (!person.softDeleted) return person
        val newCrn = additionalIdentifierRepository.findLatestMergedToCrn(person.id)?.identifier
        return newCrn?.let { personRepository.getByCrn(it) }?.takeIf { !it.softDeleted }
            .orIgnore { "Person with ${if (newCrn == null) "crn" else "mergedCrn"} of ${newCrn ?: crn} not found" }
    }

    data class ArpValues(val arpStaticDynamic: String?, val arpScore: Double?, val arpBand: String?)

    fun Ogrs4Score.arpValues(): ArpValues {
        val staticDynamic = when {
            ogp2Yr2 != null -> "D"
            ogrs4GYr2 != null -> "S"
            else -> null
        }
        val score = when {
            ogp2Yr2 != null -> ogp2Yr2
            ogrs4GYr2 != null -> ogrs4GYr2
            else -> null
        }
        val band = when {
            ogp2Yr2Band != null -> ogp2Yr2Band
            ogrs4GYr2Band != null -> ogrs4GYr2Band
            else -> null
        }
        return ArpValues(staticDynamic, score, band)
    }
}
