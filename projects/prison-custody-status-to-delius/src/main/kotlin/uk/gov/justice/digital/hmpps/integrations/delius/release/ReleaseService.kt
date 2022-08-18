package uk.gov.justice.digital.hmpps.integrations.delius.release

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventService
import uk.gov.justice.digital.hmpps.integrations.delius.institution.InstitutionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.ReleaseTypeCode
import java.time.ZonedDateTime

@Service
class ReleaseService(
    auditedInteractionService: AuditedInteractionService,
    private val eventService: EventService,
    private val institutionRepository: InstitutionRepository,
    private val referenceDataRepository: ReferenceDataRepository,
) : AuditableService(auditedInteractionService) {
    fun release(
        nomsNumber: String,
        prisonId: String,
        reason: String,
        releaseDate: ZonedDateTime,
    ) = audit(BusinessInteractionCode.ADD_RELEASE) {
        val releaseType = referenceDataRepository.getReleaseType(mapToReleaseType(reason).code)
        val institution = institutionRepository.findByNomisCdeCodeAndSelectableIsTrue(prisonId)

        eventService.getActiveCustodialEvents(nomsNumber).forEach {
            // Do the thing
        }
    }

    private fun mapToReleaseType(reason: String) = when (reason) {
        "RELEASED" -> ReleaseTypeCode.ADULT_LICENCE
        "TEMPORARY_ABSENCE_RELEASE" -> throw IgnorableMessageException("UnsupportedReleaseReason") // ReleaseTypeCode.RELEASED_ON_TEMPORARY_LICENCE
        "RELEASED_TO_HOSPITAL" -> throw IgnorableMessageException("UnsupportedReleaseReason")
        else -> throw IllegalArgumentException("Unexpected release reason: $reason")
    }
}
