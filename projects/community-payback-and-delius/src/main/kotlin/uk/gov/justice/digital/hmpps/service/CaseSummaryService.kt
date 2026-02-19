package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.person.PersonRepository
import uk.gov.justice.digital.hmpps.entity.person.getByCrn
import uk.gov.justice.digital.hmpps.entity.sentence.EventRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.LinkedListRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointmentRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UpwDetailsRepository
import uk.gov.justice.digital.hmpps.model.UnpaidWorkDetails
import uk.gov.justice.digital.hmpps.model.UnpaidWorkMinutes

@Service
class CaseSummaryService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val upwDetailsRepository: UpwDetailsRepository,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository,
    private val linkedListRepository: LinkedListRepository
) {
    fun getSummaryForCase(crn: String): UnpaidWorkDetails {
        val personId = personRepository.getByCrn(crn).id
        val events = eventRepository.getByPerson_Id(personId)
        val details = upwDetailsRepository.findByEventIdIn(events.map { it.id })
        val detailsIds = details.map { it.id }
        val requiredMinutes = if (detailsIds.isEmpty()) {
            emptyList()
        } else {
            unpaidWorkAppointmentRepository.getUpwRequiredAndCompletedMinutes(detailsIds)
        }
        val allAppointments = unpaidWorkAppointmentRepository.findByEventIdIn(events.map { it.id })
        val linkedListEntry = linkedListRepository.findLinkedListsByData1_Code("ETE")
        val eteAppts = allAppointments.filter { appointment ->
            linkedListEntry.any { it.data2.code == appointment.project.projectType.code }
        }

        val upwMinutes = details.map { detail ->
            val matchingMinutes = requiredMinutes.filter { it.id == detail.id }
            val eteMinutes = eteAppts.filter { it.details.id == detail.id }.sumOf { it.minutesCredited ?: 0 }
            UnpaidWorkMinutes(
                eventNumber = detail.disposal.event.number.toLong(),
                requiredMinutes = matchingMinutes.sumOf { it.requiredMinutes },
                adjustments = matchingMinutes.sumOf { it.positiveAdjustments - it.negativeAdjustments },
                completedMinutes = matchingMinutes.sumOf { it.completedMinutes },
                completedEteMinutes = eteMinutes
            )
        }
        return UnpaidWorkDetails(upwMinutes)
    }
}