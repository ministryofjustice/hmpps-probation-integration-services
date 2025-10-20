package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.appointment.RescheduleAppointmentRequest
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.*
import java.time.ZonedDateTime

@Service
class RescheduleAppointment(
    private val appointmentRepository: AppointmentRepository,
    private val staffRepository: AppointmentStaffRepository,
    private val teamRepository: AppointmentTeamRepository,
    private val locationRepository: AppointmentLocationRepository,
) {
    @Transactional
    fun reschedule(id: Long, request: RescheduleAppointmentRequest) {
        val appointment = appointmentRepository.getAppointment(id)
        require(appointment.isInTheFuture() && appointment.outcome == null) { "Appointment must be in the future without an outcome to reschedule" }
        require(
            request.changesDateOrTime(
                appointment.date,
                appointment.startTime.toLocalTime(),
                appointment.endTime.toLocalTime()
            )
        ) { "Appointment date or time must change to be rescheduled" }

        if (appointmentRepository.appointmentClashes(
                appointment.person.id,
                request.date,
                request.startTime,
                request.endTime,
                appointment.id
            )
        ) {
            throw ConflictException("Appointment clashes with existing appointment")
        }

        val newStaff = request.staffCode?.let { staffRepository.getByCode(it) }
        val newTeam = request.teamCode?.let { teamRepository.getByCode(it) }
        val newLocation = request.locationCode?.let { locationRepository.getByCode(it) }
        val locationNotes = newLocation?.let { new ->
            if (new.code != appointment.location?.code) {
                val setOrChanged = appointment.location?.let {
                    "changed from ${it.description}"
                } ?: "set"
                "Location $setOrChanged to ${new.description}"
            } else null
        }

        appointment.apply {
            date = request.date
            startTime = ZonedDateTime.of(date, request.startTime, EuropeLondon)
            endTime = ZonedDateTime.of(date, request.endTime, EuropeLondon)
            appendNotes(listOfNotNull(locationNotes, request.notes))
            newStaff?.also { staff = it }
            newTeam?.also { team = it }
            newLocation?.also { location = it }
            request.sensitive?.let { amendmentSensitive(it) }
        }
    }
}