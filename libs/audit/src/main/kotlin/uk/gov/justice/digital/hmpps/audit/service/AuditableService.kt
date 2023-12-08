package uk.gov.justice.digital.hmpps.audit.service

import uk.gov.justice.digital.hmpps.audit.InteractionCode
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction

abstract class AuditableService(private val auditedInteractionService: AuditedInteractionService) {
    protected fun <T> audit(
        interactionCode: InteractionCode,
        params: AuditedInteraction.Parameters = AuditedInteraction.Parameters(),
        code: (AuditedInteraction.Parameters) -> T,
    ): T {
        try {
            val result = code(params)
            auditedInteractionService.createAuditedInteraction(
                interactionCode,
                params,
                AuditedInteraction.Outcome.SUCCESS,
            )
            return result
        } catch (e: Exception) {
            auditedInteractionService.createAuditedInteraction(
                interactionCode,
                params,
                AuditedInteraction.Outcome.FAIL,
            )
            throw e
        }
    }
}
