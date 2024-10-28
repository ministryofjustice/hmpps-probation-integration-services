package uk.gov.justice.digital.hmpps.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import java.time.ZonedDateTime

@Service
class AppointmentService(
    auditedInteractionService: AuditedInteractionService,
    private val appointmentRepository: AppointmentRepository,
    private val appointmentTypeRepository: AppointmentTypeRepository,
    private val alertRepository: AlertRepository,
    private val offenderManagerRepository: OffenderManagerRepository
) : AuditableService(auditedInteractionService) {

    fun createAppointment( crn: String,
        createAppointment: CreateAppointment
    ) = audit(BusinessInteractionCode.ADD_CONTACT) { audit ->
        val om = offenderManagerRepository.getByCrn(crn)
        checkForConflicts(om.person.id, createAppointment)
        val appointment = appointmentRepository.save(createAppointment.withManager(om))
        alertRepository.save(appointment.alert(om))
        audit["offenderId"] = om.person.id
        audit["contactId"] = appointment.id
    }

    private fun checkForConflicts(
        personId: Long,
        createAppointment: CreateAppointment
    ) {

        if (createAppointment.duration.isNegative) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment end time cannot be before start time.")
        }
        if (createAppointment.start.isAfter(ZonedDateTime.now()) && appointmentRepository.appointmentClashes(
                personId,
                createAppointment.start.toLocalDate(),
                createAppointment.start,
                createAppointment.end,
            )
        ) {
            throw ConflictException("Appointment conflicts with an existing future appointment")
        }
    }

    private fun CreateAppointment.withManager(om: OffenderManager) = Appointment(
        om.person,
        appointmentTypeRepository.getByCode(type.code),
        start.toLocalDate(),
        start,
        end,
        notes,
        om.provider.id,
        om.team,
        om.staff,
        urn
    )

    private fun Appointment.alert(personManager: OffenderManager) = Alert(
        id, type.id, person.id, personManager.team.id, personManager.staff.id, personManager.id
    )
}