package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteRelatedIds
import uk.gov.justice.digital.hmpps.integrations.delius.repository.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.NsiRepository

@Service
class CaseNoteRelatedService(
    private val eventRepository: EventRepository,
    private val nsiRepository: NsiRepository,
) {
    fun findRelatedCaseNoteIds(
        offenderId: Long,
        cnTypeCode: String,
    ): CaseNoteRelatedIds {
        val nsi = nsiRepository.findCaseNoteRelatedNsis(offenderId, cnTypeCode).firstOrNull()
        return if (nsi == null) {
            val eventIds = eventRepository.findActiveCustodialEvents(offenderId)
            if (eventIds.size == 1) CaseNoteRelatedIds(eventId = eventIds[0]) else CaseNoteRelatedIds()
        } else {
            CaseNoteRelatedIds(nsi.eventId, nsi.id)
        }
    }
}
