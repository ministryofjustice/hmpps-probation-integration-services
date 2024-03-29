package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.ogrs.OASYSAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.ogrs.OGRSAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrnAndSoftDeletedFalse
import uk.gov.justice.digital.hmpps.integrations.delius.registration.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.registration.entity.Registration

@Service
class AllocationRiskService(
    private val registrationRepository: RegistrationRepository,
    private val ogrsAssessmentRepository: OGRSAssessmentRepository,
    private val oasysAssessmentRepository: OASYSAssessmentRepository,
    private val personRepository: PersonRepository
) {

    fun getRiskRecord(crn: String): RiskRecord {
        val person = personRepository.getByCrnAndSoftDeletedFalse(crn)
        val registrations = registrationRepository.findAllByPersonCrn(crn)

        val riskRegistrations = registrations.map { it.forRisk() }.groupBy { it.endDate == null }
        val riskOGRS = getRiskOgrs(person)

        return RiskRecord(
            person.crn,
            person.name(),
            riskRegistrations[true] ?: listOf(),
            riskRegistrations[false] ?: listOf(),
            riskOGRS
        )
    }

    fun getRiskOgrs(person: Person): RiskOGRS? {
        val oasysAssessment = oasysAssessmentRepository.findFirstByPersonIdAndScoreIsNotNullOrderByAssessmentDateDesc(
            person.id
        )
        val ogrsAssessment =
            ogrsAssessmentRepository.findFirstByEventPersonIdAndScoreIsNotNullOrderByAssessmentDateDesc(person.id)
        val assessment = listOfNotNull(oasysAssessment, ogrsAssessment).maxByOrNull { it.assessmentDate }
        return assessment?.let {
            RiskOGRS(assessment.lastModifiedDateTime.toLocalDate(), assessment.score)
        }
    }

    fun Registration.forRisk() = RiskRegistration(
        registerType.description,
        startDate,
        endDate,
        notes,
        registerType.flag?.description?.let(::RegistrationFlag)
    )
}
