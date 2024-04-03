package uk.gov.justice.digital.hmpps.audit.service

import uk.gov.justice.digital.hmpps.audit.InteractionCode
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction
import java.time.ZonedDateTime

abstract class AuditableService(private val auditedInteractionService: AuditedInteractionService) {
    protected fun <T> audit(
        interactionCode: InteractionCode,
        dateTime: ZonedDateTime = ZonedDateTime.now(),
        username: String? = null,
        params: AuditedInteraction.Parameters = AuditedInteraction.Parameters(),
        code: (AuditedInteraction.Parameters) -> T
    ): T {
        try {
            val result = code(params)
            auditedInteractionService.createAuditedInteraction(
                interactionCode,
                params,
                AuditedInteraction.Outcome.SUCCESS,
                dateTime,
                username
            )
            return result
        } catch (e: Exception) {
            auditedInteractionService.createAuditedInteraction(
                interactionCode,
                params,
                AuditedInteraction.Outcome.FAIL,
                dateTime,
                username
            )
            throw e
        }
    }
}
