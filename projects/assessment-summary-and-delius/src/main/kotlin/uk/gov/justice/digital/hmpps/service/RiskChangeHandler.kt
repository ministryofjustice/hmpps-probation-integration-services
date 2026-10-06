package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.audit.service.OptimisationTables
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.UPDATE_RISK_DATA
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getByCrn
import uk.gov.justice.digital.hmpps.integrations.oasys.RiskAssessment
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
@Transactional
class RiskChangeHandler(
    auditedInteractionService: AuditedInteractionService,
    private val personRepository: PersonRepository,
    private val riskService: RiskService,
    private val telemetryService: TelemetryService,
    private val optimisationTables: OptimisationTables,
) : AuditableService(auditedInteractionService) {
    fun riskChange(crn: String, summary: RiskAssessment) {
        val telemetryParams = mutableMapOf(
            "crn" to crn,
            "dateCompleted" to summary.dateCompleted.toString(),
            "assessmentType" to summary.assessmentType,
            "assessmentStatus" to summary.assessmentStatus,
            "assessmentId" to summary.assessmentPk.toString(),
            "ROSH" to summary.riskLevel.riskScoreLevel?.toString(),
        )

        val person = personRepository.getByCrn(crn)

        if (summary.assessmentStatus != "COMPLETE") audit(UPDATE_RISK_DATA) {
            it["CRN"] = person.crn
            val ta = TelemetryAggregator()
            riskService.recordRiskOfSeriousHarm(person, summary) { key, value -> ta.add(key, value) }
            telemetryParams.putAll(ta.params())
        }

        optimisationTables.rebuild(person.id)
        telemetryService.trackEvent("RiskChangeSuccess", telemetryParams)
    }
}
