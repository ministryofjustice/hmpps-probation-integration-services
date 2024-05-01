package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.delius.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.getByCrnOrNoms
import uk.gov.justice.digital.hmpps.messaging.OpdAssessment

@Service
class OpdService(
    private val personManagerRepository: PersonManagerRepository,
    private val eventRepository: EventRepository,
    private val nsiService: NsiService
) {
    @Transactional
    fun processAssessment(opdAssessment: OpdAssessment) {
        val com = personManagerRepository.getByCrnOrNoms(opdAssessment.crn, opdAssessment.noms)
        val activeEvent = eventRepository.existsByPersonId(com.person.id)
        val nsi = nsiService.findOpdNsi(com.person.id)

        when {
            activeEvent && nsi?.active != true -> {
                nsiService.createNsi(opdAssessment, com)
            }

            nsi?.active == true -> {
                nsi.appendNotes(System.lineSeparator() + opdAssessment.notes)
            }
        }
    }
}
