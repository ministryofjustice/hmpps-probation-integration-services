package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.audit.service.OptimisationTables
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
class AssessmentSubmittedHandler(
    auditedInteractionService: AuditedInteractionService,
    private val personRepository: PersonRepository,
    private val assessmentService: AssessmentService,
    private val riskService: RiskService,
    private val telemetryService: TelemetryService,
    private val optimisationTables: OptimisationTables,
    private val domainEventService: DomainEventService,
) : AuditableService(auditedInteractionService) {
    companion object {
        const val UPDATE_RISK_REGISTRATIONS_IN_PLACE = "assessment-summary_update-risk-registrations-in-place"
    }

    fun assessmentSubmitted(crn: String, summary: AssessmentSummary) {
        val telemetryParams = mutableMapOf(
            "crn" to crn,
            "dateCompleted" to summary.dateCompleted.toString(),
            "assessmentType" to summary.assessmentType,
            "assessmentStatus" to summary.assessmentStatus,
            "assessmentId" to summary.assessmentPk.toString(),
            "ROSH" to summary.riskFlags.mapNotNull(RiskOfSeriousHarmType::of).maxByOrNull { it.ordinal }.toString(),
        )

        telemetryParams.putAll(RiskType.entries.map { it.name to it.riskLevel(summary)?.name.toString() })

        val person = personRepository.getByCrn(crn)

        val contact = audit(SUBMIT_ASSESSMENT_SUMMARY) {
            it["CRN"] = person.crn
            it["OASysId"] = summary.assessmentPk
            assessmentService.recordAssessment(person, summary)
        }

        if (summary.assessmentStatus == "COMPLETE") audit(UPDATE_RISK_DATA) {
            it["CRN"] = person.crn
            val ta = TelemetryAggregator()
            riskService.recordRisk(person, summary) { key, value -> ta.add(key, value) }
            telemetryParams.putAll(ta.params())
        }

        contact.copyToVisor = riskService.activeVisorAndMappa(person)

        if (contact.copyToVisor == true) {
            domainEventService.publishVisorContact(person, contact.id!!)
        }

        if (personRepository.countAccreditedProgrammeRequirements(person.id) > 0) {
            personRepository.updateIaps(person.id)
        }

        optimisationTables.rebuild(person.id)

        telemetryService.trackEvent("AssessmentSummarySuccess", telemetryParams)
    }
}
