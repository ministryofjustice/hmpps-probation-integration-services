package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteRelatedIds
import uk.gov.justice.digital.hmpps.integrations.delius.repository.EventRepository

@Service
class CaseNoteRelatedService(
    private val eventRepository: EventRepository
) {
    fun findRelatedCaseNoteIds(offenderId: Long): CaseNoteRelatedIds {
        val eventIds = eventRepository.findActiveCustodialEvents(offenderId)
        return if (eventIds.size == 1) CaseNoteRelatedIds(eventId = eventIds[0]) else CaseNoteRelatedIds()
    }
}
