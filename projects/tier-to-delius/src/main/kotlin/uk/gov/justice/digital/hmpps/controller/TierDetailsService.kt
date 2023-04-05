package uk.gov.justice.digital.hmpps.controller

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.entity.CaseEntity
import uk.gov.justice.digital.hmpps.controller.entity.CaseRepository
import uk.gov.justice.digital.hmpps.controller.entity.EventEntity
import uk.gov.justice.digital.hmpps.controller.entity.EventRepository
import uk.gov.justice.digital.hmpps.controller.entity.OASYSAssessmentRepository
import uk.gov.justice.digital.hmpps.controller.entity.OGRSAssessmentRepository
import uk.gov.justice.digital.hmpps.controller.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.controller.entity.getCase
import uk.gov.justice.digital.hmpps.controller.model.Conviction
import uk.gov.justice.digital.hmpps.controller.model.Registration
import uk.gov.justice.digital.hmpps.controller.model.Requirement
import uk.gov.justice.digital.hmpps.controller.model.TierDetails

@Service
class TierDetailsService(
    val caseRepository: CaseRepository,
    val registrationRepository: RegistrationRepository,
    val eventRepository: EventRepository,
    val oasysAssessmentRepository: OASYSAssessmentRepository,
    val ogrsAssessmentRepository: OGRSAssessmentRepository

) {
    fun tierDetails(crn: String): TierDetails {
        val case = caseRepository.getCase(crn)
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
            convictions
        )
    }

    private fun mapToConvictions(eventEntities: List<EventEntity>) = eventEntities.mapNotNull { event ->
        event.disposal?.let { disposal ->
            Conviction(
                disposal.terminationDate,
                disposal.disposalType.description,
                event.inBreach,
                disposal.requirements.map {
                    Requirement(it.mainCategory.code, it.mainCategory.restrictive)
                }
            )
        }
    }

    private fun getRiskOgrs(case: CaseEntity): Int? {
        val oasysAssessment = oasysAssessmentRepository.findFirstByPersonIdAndScoreIsNotNullOrderByAssessmentDateDesc(
            case.id
        )
        val ogrsAssessment =
            ogrsAssessmentRepository.findFirstByEventPersonIdAndScoreIsNotNullOrderByAssessmentDateDesc(case.id)
        val assessment = listOfNotNull(oasysAssessment, ogrsAssessment).maxByOrNull { it.assessmentDate }
        return assessment?.let {
            assessment.score
        }
    }
}