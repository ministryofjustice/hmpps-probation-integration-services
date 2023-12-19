package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.SUBMIT_ASSESSMENT_SUMMARY
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.UPDATE_RISK_DATA
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getByCrn
import uk.gov.justice.digital.hmpps.integrations.oasys.AssessmentSummary

@Service
@Transactional
class AssessmentSubmitted(
    auditedInteractionService: AuditedInteractionService,
    private val personRepository: PersonRepository,
    private val assessmentService: AssessmentService,
    private val riskService: RiskService,
    private val domainEventService: DomainEventService
) : AuditableService(auditedInteractionService) {
    fun assessmentSubmitted(crn: String, summary: AssessmentSummary) {
        val person = personRepository.getByCrn(crn)

        audit(SUBMIT_ASSESSMENT_SUMMARY) {
            it["CRN"] = person.crn
            it["OASysId"] = summary.assessmentPk
            assessmentService.recordAssessment(person, summary)
        }

        val regEvents = audit(UPDATE_RISK_DATA) {
            it["CRN"] = person.crn
            riskService.recordRisk(person, summary)
        }

        if (personRepository.countAccreditedProgrammeRequirements(person.id) > 0) {
            personRepository.updateIaps(person.id)
        }
        domainEventService.publishEvents(regEvents)
    }
}
