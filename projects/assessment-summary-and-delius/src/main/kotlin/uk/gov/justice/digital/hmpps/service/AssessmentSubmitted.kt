package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.enum.RiskOfSeriousHarmType
import uk.gov.justice.digital.hmpps.enum.RiskType
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.SUBMIT_ASSESSMENT_SUMMARY
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.UPDATE_RISK_DATA
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getByCrn
import uk.gov.justice.digital.hmpps.integrations.oasys.AssessmentSummary
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
@Transactional
class AssessmentSubmitted(
    auditedInteractionService: AuditedInteractionService,
    private val personRepository: PersonRepository,
    private val assessmentService: AssessmentService,
    private val riskService: RiskService,
    private val domainEventService: DomainEventService,
    private val telemetryService: TelemetryService
) : AuditableService(auditedInteractionService) {
    fun assessmentSubmitted(crn: String, summary: AssessmentSummary) {
        val telemetryParams = mapOf(
            "crn" to crn,
            "dateCompleted" to summary.dateCompleted.toString(),
            "assessmentType" to summary.assessmentType,
            "assessmentId" to summary.assessmentPk.toString(),
            "ROSH" to summary.riskFlags.mapNotNull(RiskOfSeriousHarmType::of).maxByOrNull { it.ordinal }.toString(),
        ) + RiskType.entries.map {
            it.name to it.riskLevel(summary)?.name.toString()
        }

        val person = personRepository.getByCrn(crn)

        audit(SUBMIT_ASSESSMENT_SUMMARY) {
            it["CRN"] = person.crn
            it["OASysId"] = summary.assessmentPk
            assessmentService.recordAssessment(person, summary)
        }

        if (summary.assessmentStatus == "COMPLETE") audit(UPDATE_RISK_DATA) {
            it["CRN"] = person.crn
            val registrationEvents = riskService.recordRisk(person, summary)
            domainEventService.publishEvents(registrationEvents)
        }

        if (personRepository.countAccreditedProgrammeRequirements(person.id) > 0) {
            personRepository.updateIaps(person.id)
        }

        telemetryService.trackEvent("AssessmentSummarySuccess", telemetryParams)
    }
}
