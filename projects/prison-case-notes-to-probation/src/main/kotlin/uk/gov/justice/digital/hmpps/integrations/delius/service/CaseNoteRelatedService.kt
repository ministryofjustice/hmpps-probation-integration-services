package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteRelatedIds
import uk.gov.justice.digital.hmpps.integrations.delius.model.isAlertType
import uk.gov.justice.digital.hmpps.integrations.delius.repository.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.repository.NsiRepository

@Service
class CaseNoteRelatedService(
    private val eventRepository: EventRepository,
    private val nsiRepository: NsiRepository
) {
    fun findRelatedCaseNoteIds(offenderId: Long, cnTypeCode: String): CaseNoteRelatedIds {
        if (cnTypeCode.isAlertType()) return CaseNoteRelatedIds()

        val nsi = nsiRepository.findCaseNoteRelatedNsis(offenderId, cnTypeCode).firstOrNull()
        if (nsi != null) return CaseNoteRelatedIds(nsi.eventId, nsi.id)

        val eventId = eventRepository.findActiveCustodialEvents(offenderId).singleOrNull()
        if (eventId != null) return CaseNoteRelatedIds(eventId = eventId)

        return CaseNoteRelatedIds()
    }
}
