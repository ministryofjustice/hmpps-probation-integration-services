package uk.gov.justice.digital.hmpps.audit.service

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionNotFoundException
import uk.gov.justice.digital.hmpps.audit.InteractionCode
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.security.ServiceContext

@Service
class AuditedInteractionService(
    private val businessInteractionRepository: BusinessInteractionRepository,
    private val auditedInteractionRepository: AuditedInteractionRepository,
) {
    @Async
    @Transactional
    fun createAuditedInteraction(
        interactionCode: InteractionCode,
        params: AuditedInteraction.Parameters,
        outcome: AuditedInteraction.Outcome,
    ) {
        ServiceContext.servicePrincipal()!!.let {
            val bi = businessInteractionRepository.findByCode(interactionCode.code)
            auditedInteractionRepository.save(
                AuditedInteraction(
                    bi?.id ?: throw BusinessInteractionNotFoundException(interactionCode.code),
                    it.userId,
                    parameters = params,
                    outcome = outcome,
                ),
            )
        }
    }
}
