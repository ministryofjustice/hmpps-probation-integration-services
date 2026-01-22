package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.person.PersonRepository
import uk.gov.justice.digital.hmpps.entity.person.getByCrn
import uk.gov.justice.digital.hmpps.entity.sentence.EventRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointmentRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UpwAllocationRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UpwDetailsRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UpwMinutes
import uk.gov.justice.digital.hmpps.model.ScheduleResponse
import uk.gov.justice.digital.hmpps.model.toAllocationResponse
import uk.gov.justice.digital.hmpps.model.toAppointmentScheduleResponse

@Service
class CaseScheduleService(
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val upwDetailsRepository: UpwDetailsRepository,
    private val upwAllocationRepository: UpwAllocationRepository,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository
) {
    fun getSchedule(crn: String, eventNumber: String): ScheduleResponse {
        val person = personRepository.getByCrn(crn)
        val event = eventRepository.getByPersonAndEventNumber(person.id, eventNumber)
        val upwDetailsIds = upwDetailsRepository.findByEventId(event.id).map { it.id }
        val requirementProgress = if (upwDetailsIds.isNotEmpty()) {
            val upwMinutesDtos = unpaidWorkAppointmentRepository.getUpwRequiredAndCompletedMinutes(upwDetailsIds)

            UpwMinutes(
                requiredMinutes = upwMinutesDtos.sumOf { it.requiredMinutes },
                completedMinutes = upwMinutesDtos.sumOf { it.completedMinutes },
                adjustments = upwMinutesDtos.sumOf { it.positiveAdjustments - it.negativeAdjustments }
            )
        } else {
            UpwMinutes(0, 0, 0)
        }

        val allocations = upwAllocationRepository.findByEventId(event.id)
            .map { it.toAllocationResponse() }
        val appointments = unpaidWorkAppointmentRepository.findByEventId(event.id)
            .map { it.toAppointmentScheduleResponse() }

        return ScheduleResponse(
            requirementProgress = requirementProgress,
            allocations = allocations,
            appointments = appointments
        )
    }
}
