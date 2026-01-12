package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
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

            var requiredMinutes: Long = 0
            var completedMinutes: Long = 0
            var adjustments: Long = 0
            upwMinutesDtos.forEach {
                requiredMinutes += it.requiredMinutes
                completedMinutes += it.completedMinutes
                adjustments += it.positiveAdjustments - it.negativeAdjustments
            }

            UpwMinutes(
                requiredMinutes = requiredMinutes,
                completedMinutes = completedMinutes,
                adjustments = adjustments
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
