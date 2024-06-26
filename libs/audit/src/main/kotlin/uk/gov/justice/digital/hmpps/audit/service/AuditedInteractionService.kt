package uk.gov.justice.digital.hmpps.audit.service

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.InteractionCode
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.audit.repository.BusinessInteractionRepository
import uk.gov.justice.digital.hmpps.audit.repository.getByCode
import uk.gov.justice.digital.hmpps.security.ServiceContext
import uk.gov.justice.digital.hmpps.user.AuditUserService
import java.time.ZonedDateTime

@Service
class AuditedInteractionService(
    private val businessInteractionRepository: BusinessInteractionRepository,
    private val auditedInteractionRepository: AuditedInteractionRepository,
    private val auditUserService: AuditUserService
) {
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createAuditedInteraction(
        interactionCode: InteractionCode,
        params: AuditedInteraction.Parameters,
        outcome: AuditedInteraction.Outcome,
        dateTime: ZonedDateTime,
        username: String?
    ) {
        val bi = businessInteractionRepository.getByCode(interactionCode.code)
        auditedInteractionRepository.save(
            AuditedInteraction(
                businessInteractionId = bi.id,
                userId = username?.let { auditUserService.findUser(it) }?.id
                    ?: ServiceContext.servicePrincipal()!!.userId,
                dateTime = dateTime,
                parameters = params,
                outcome = outcome
            )
        )
    }
}
