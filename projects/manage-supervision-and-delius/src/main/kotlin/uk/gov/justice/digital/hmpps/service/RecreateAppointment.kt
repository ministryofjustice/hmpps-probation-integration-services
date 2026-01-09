package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.appointment.RecreateAppointmentRequest
import uk.gov.justice.digital.hmpps.api.model.appointment.RecreateAppointmentRequest.RequestedBy.POP
import uk.gov.justice.digital.hmpps.api.model.appointment.RecreateAppointmentRequest.RequestedBy.SERVICE
import uk.gov.justice.digital.hmpps.api.model.appointment.RecreatedAppointment
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.*
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.Appointment.Companion.URN_PREFIX
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.AppointmentOutcome.Code.ATTENDED_COMPLIED
import java.time.ZonedDateTime

@Service
class RecreateAppointment(
    private val appointmentRepository: AppointmentRepository,
    private val outcomeRepository: AppointmentOutcomeRepository,
    private val staffRepository: AppointmentStaffRepository,
    private val teamRepository: AppointmentTeamRepository,
    private val locationRepository: AppointmentLocationRepository,
) {
    @Transactional
    fun recreate(id: Long, request: RecreateAppointmentRequest): RecreatedAppointment {
        val original = appointmentRepository.getAppointment(id)
        require(original.outcome == null) { "Appointment with an outcome cannot be recreated" }
        require(
            request.changesDateOrTime(
                original.date,
                original.startTime?.toLocalTime(),
                original.endTime?.toLocalTime()
            )
        ) { "Appointment date or time must change to be recreated" }

        require(request.isInFuture() || request.outcomeRecorded) { "Appointments in the past require an outcome" }

        request.reasonForRecreate?.also { reason ->
            original.appendNotes(reason)
            request.reasonIsSensitive?.also { original.amendmentSensitive(it) }
        }

        val newAppointment = appointmentRepository.save(original.recreateWith(request, original.eventId))
        original.applyOutcome(
            when (request.requestedBy) {
                POP -> outcomeRepository.getByCode(AppointmentOutcome.Code.RESCHEDULED_POP.value)
                SERVICE -> outcomeRepository.getByCode(AppointmentOutcome.Code.RESCHEDULED_SERVICE.value)
            }
        )
        return RecreatedAppointment(newAppointment.id!!, requireNotNull(newAppointment.externalReference))
    }

    private fun Appointment.recreateWith(request: RecreateAppointmentRequest, eventId: Long): Appointment {
        val team = request.teamCode?.let { teamRepository.getByCode(it) } ?: team
        val (location, locationNotes) = request.locationCode?.let { locationCode ->
            if (locationCode != location?.code) {
                val newLocation = locationRepository.getByCode(team.provider.code, locationCode)
                val notes = newLocation.appointmentNotes(location)
                newLocation to notes
            } else null
        } ?: (location to null)

        return Appointment(
            person = person,
            type = type,
            staff = request.staffCode?.let { staffRepository.getByCode(it) } ?: staff,
            team = team,
            location = location,
            date = request.date,
            startTime = ZonedDateTime.of(request.date, request.startTime, EuropeLondon),
            endTime = ZonedDateTime.of(request.date, request.endTime, EuropeLondon),
            provider = team.provider,
            outcome = if (request.outcomeRecorded) {
                outcomeRepository.getByCode(ATTENDED_COMPLIED.value)
            } else null,
            rarActivity = rarActivity,
            notes = notes,
            sensitive = sensitive == true || request.sensitive == true,
            sendToVisor = request.sendToVisor,
            externalReference = request.uuid?.let { URN_PREFIX + it },
            softDeleted = false,
            eventId = eventId
        ).appendNotes(listOfNotNull(locationNotes, request.notes))
    }
}