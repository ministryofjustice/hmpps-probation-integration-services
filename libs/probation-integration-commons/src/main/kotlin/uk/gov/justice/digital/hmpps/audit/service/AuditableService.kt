package uk.gov.justice.digital.hmpps.audit.service

import uk.gov.justice.digital.hmpps.audit.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.InteractionCode

abstract class AuditableService(protected val auditedInteractionService: AuditedInteractionService) {
    fun <T> audit(
        interactionCode: InteractionCode,
        params: AuditedInteraction.Parameters = AuditedInteraction.Parameters(),
        code: (AuditedInteraction.Parameters) -> T
    ): T {
        try {
            val result = code(params)
            auditedInteractionService.createAuditedInteraction(
                interactionCode,
                params,
                AuditedInteraction.Outcome.SUCCESS
            )
            return result
        } catch (e: Exception) {
            auditedInteractionService.createAuditedInteraction(
                interactionCode,
                params,
                AuditedInteraction.Outcome.FAIL
            )
            throw e
        }
    }
}
