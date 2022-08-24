package uk.gov.justice.digital.hmpps.integrations.delius.recall

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventService
import uk.gov.justice.digital.hmpps.integrations.delius.institution.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.institution.getByNomisCdeCodeAndIdEstablishmentIsTrue
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.RecallReasonCode
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.RecallReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.recall.reason.getByCodeAndSelectableIsTrue
import java.time.ZonedDateTime

@Service
class RecallService(
    auditedInteractionService: AuditedInteractionService,
    private val eventService: EventService,
    private val institutionRepository: InstitutionRepository,
    private val recallReasonRepository: RecallReasonRepository,
) : AuditableService(auditedInteractionService) {

    @Transactional
    fun recall(
        nomsNumber: String,
        prisonId: String,
        reason: String,
        releaseDate: ZonedDateTime,
    ) = audit(BusinessInteractionCode.ADD_RECALL) {
        val recallReason = recallReasonRepository.getByCodeAndSelectableIsTrue(mapToRecallReason(reason).code)
        val institution = institutionRepository.getByNomisCdeCodeAndIdEstablishmentIsTrue(prisonId)

        eventService.getActiveCustodialEvents(nomsNumber).forEach {
            // Do the thing
        }
    }

    private fun mapToRecallReason(reason: String) = when (reason) {
        "ADMISSION" -> RecallReasonCode.NOTIFIED_BY_CUSTODIAL_ESTABLISHMENT
        "TEMPORARY_ABSENCE_RETURN" -> throw IgnorableMessageException("UnsupportedRecallReason") // RecallReasonCode.END_OF_TEMPORARY_LICENCE
        else -> throw IllegalArgumentException("Unexpected recall reason: $reason")
    }
}
