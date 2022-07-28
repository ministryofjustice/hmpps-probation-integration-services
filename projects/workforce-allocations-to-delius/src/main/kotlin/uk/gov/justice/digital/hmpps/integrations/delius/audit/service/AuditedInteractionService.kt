package uk.gov.justice.digital.hmpps.integrations.delius.audit.service

import org.springframework.scheduling.annotation.Async
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.config.security.ServicePrincipal
import uk.gov.justice.digital.hmpps.exceptions.BusinessInteractionNotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.AuditedInteraction
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.audit.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.audit.repository.BusinessInteractionRepository

@Service
class AuditedInteractionService(
    private val businessInteractionRepository: BusinessInteractionRepository,
    private val auditedInteractionRepository: AuditedInteractionRepository,
) {
    @Async
    @Transactional
    fun createAuditedInteraction(biCode: BusinessInteractionCode, params: AuditedInteraction.Parameters) {
        val principal = SecurityContextHolder.getContext().authentication?.principal

        if (principal is ServicePrincipal) {
            val bi = businessInteractionRepository.findByCode(biCode.code)
            auditedInteractionRepository.save(
                AuditedInteraction(
                    bi?.id ?: throw BusinessInteractionNotFoundException(biCode.code),
                    principal.userId ?: throw IllegalArgumentException("No user id in security context"),
                    parameters = params
                )
            )
        }
    }
}
