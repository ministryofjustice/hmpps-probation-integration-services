package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.RiskOGRS
import uk.gov.justice.digital.hmpps.api.model.RiskRecord
import uk.gov.justice.digital.hmpps.api.model.RiskRegistration
import uk.gov.justice.digital.hmpps.api.model.name
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.ogrs.OASYSAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.ogrs.OGRSAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.registration.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.event.registration.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrnAndSoftDeletedFalse

@Service
class AllocationRiskService(
    private val registrationRepository: RegistrationRepository,
    private val ogrsAssessmentRepository: OGRSAssessmentRepository,
    private val oasysAssessmentRepository: OASYSAssessmentRepository,
    private val eventRepository: EventRepository,
    private val personRepository: PersonRepository
) {

    fun getRiskRecord(crn: String, eventNumber: String): RiskRecord {
        val person = personRepository.getByCrnAndSoftDeletedFalse(crn)
        val registrations = registrationRepository.findAllByPersonCrn(crn)

        val riskRegistrations = registrations.map { it.forRisk() }.groupBy { it.endDate == null }
        val riskOGRS = getRiskOgrs(person, eventNumber)

        return RiskRecord(
            person.crn,
            person.name(),
            riskRegistrations[true] ?: listOf(),
            riskRegistrations[false] ?: listOf(),
            riskOGRS
        )
    }

    private fun getRiskOgrs(person: Person, eventNumber: String): RiskOGRS? {
        val event = eventRepository.findByPersonCrnAndNumber(person.crn, eventNumber) ?: throw NotFoundException(
            "Event",
            "crn",
            person.crn
        )
        val oasysAssessment = oasysAssessmentRepository.findByPersonIdAndEventNumberOrderByAssessmentDateDesc(
            person.id,
            eventNumber
        )
        val ogrsAssessment = ogrsAssessmentRepository.findByEventIdOrderByAssessmentDateDesc(event.id)
        val assessment = listOfNotNull(oasysAssessment, ogrsAssessment).maxByOrNull { it.assessmentDate }
        return assessment?.let {
            RiskOGRS(assessment.lastModifiedDateTime.toLocalDate(), assessment.score)
        }
    }

    fun Registration.forRisk() = RiskRegistration(registerType.description, startDate, endDate, notes)
}
