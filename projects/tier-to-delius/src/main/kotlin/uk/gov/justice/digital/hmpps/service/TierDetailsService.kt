package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.model.Conviction
import uk.gov.justice.digital.hmpps.controller.model.Registration
import uk.gov.justice.digital.hmpps.controller.model.Requirement
import uk.gov.justice.digital.hmpps.controller.model.TierDetails
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventEntity
import uk.gov.justice.digital.hmpps.integrations.delius.nsi.entity.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nsi.entity.previousEnforcementActivity
import uk.gov.justice.digital.hmpps.integrations.delius.oasys.assessment.OASYSAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.oasys.assessment.findLatest
import uk.gov.justice.digital.hmpps.integrations.delius.oasys.ogrs.OGRSAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.oasys.ogrs.findLatest
import uk.gov.justice.digital.hmpps.integrations.delius.oasys.rsr.RsrScoreHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.CaseEntity
import uk.gov.justice.digital.hmpps.integrations.delius.person.CaseEntityRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getCase
import uk.gov.justice.digital.hmpps.integrations.delius.registration.RegistrationRepository

@Service
class TierDetailsService(
    val caseEntityRepository: CaseEntityRepository,
    val registrationRepository: RegistrationRepository,
    val eventRepository: EventRepository,
    val oasysAssessmentRepository: OASYSAssessmentRepository,
    val ogrsAssessmentRepository: OGRSAssessmentRepository,
    val nsiRepository: NsiRepository
) {
    fun tierDetails(crn: String): TierDetails {
        val case = caseEntityRepository.getCase(crn)
        val registrationEntities = registrationRepository.findByPersonIdOrderByDateDesc(case.id)
        val eventEntities = eventRepository.findByCrn(crn)
        val convictions = mapToConvictions(eventEntities)
        val ogrsScore = getRiskOgrs(case)

        return TierDetails(
            case.gender.description,
            case.tier?.code,
            ogrsScore,
            case.dynamicRsrScore,
            registrationEntities.map { Registration(it.type.code, it.type.description, it.level?.code, it.date) },
            convictions,
            nsiRepository.previousEnforcementActivity(case.id)
        )
    }

    private fun mapToConvictions(eventEntities: List<EventEntity>) = eventEntities.mapNotNull { event ->
        event.disposal?.let { disposal ->
            Conviction(
                disposal.terminationDate,
                disposal.disposalType.sentenceType,
                event.inBreach,
                disposal.requirements.mapNotNull { rq ->
                    rq.mainCategory?.code?.let { Requirement(it, rq.mainCategory.restrictive) }
                }
            )
        }
    }

    private fun getRiskOgrs(case: CaseEntity): Long? {
        val oasysAssessment = oasysAssessmentRepository.findLatest(case.id)
        val ogrsAssessment = ogrsAssessmentRepository.findLatest(case.id)
        return listOfNotNull(oasysAssessment, ogrsAssessment)
            .filter { it.score != null }
            .maxByOrNull { it.assessmentDate }?.score
    }
}
