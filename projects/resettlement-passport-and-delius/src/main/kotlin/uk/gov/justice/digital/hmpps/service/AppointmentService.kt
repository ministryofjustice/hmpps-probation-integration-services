package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.api.model.Appointment
import uk.gov.justice.digital.hmpps.api.model.Location
import uk.gov.justice.digital.hmpps.api.model.Staff
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.ldap.findByUsername
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

@Service
class AppointmentService(
    auditedInteractionService: AuditedInteractionService,
    private val appointmentRepository: AppointmentRepository,
    private val appointmentTypeRepository: AppointmentTypeRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val alertRepository: AlertRepository,
    private val ldap: LdapTemplate
) : AuditableService(auditedInteractionService) {
    fun findAppointmentsFor(
        crn: String,
        startDate: LocalDate,
        endDate: LocalDate,
        pageable: Pageable
    ) = appointmentRepository.findAppointmentsFor(crn, startDate, endDate, pageable).map { it.asAppointment() }

    fun createAppointment(
        crn: String,
        createAppointment: CreateAppointment
    ) = audit(BusinessInteractionCode.ADD_CONTACT) {
        val pm = personManagerRepository.getByCrn(crn)
        checkForConflicts(pm.person.id, createAppointment)
        val appointment = appointmentRepository.save(createAppointment.withManager(pm))
        alertRepository.save(appointment.alert(pm))
    }

    private fun uk.gov.justice.digital.hmpps.entity.Appointment.asAppointment(): Appointment =
        Appointment(
            Appointment.Type(type.code, type.description),
            ZonedDateTime.of(date, startTime?.toLocalTime() ?: LocalTime.MIDNIGHT, EuropeLondon),
            duration,
            staff.asStaff(),
            location?.asLocation(),
            description ?: type.description,
            outcome?.let { Appointment.Outcome(it.code, it.description) }
        )

    private fun uk.gov.justice.digital.hmpps.entity.Staff.asStaff(): Staff {
        val userDetails = user?.let { ldap.findByUsername<LdapUserDetails>(it.username) }
        return Staff(code, Name(forename, surname), userDetails?.email, userDetails?.telephone)
    }

    private fun uk.gov.justice.digital.hmpps.entity.Location.asLocation(): Location =
        Location(
            code,
            description,
            Address.from(buildingName, buildingNumber, streetName, district, townCity, county, postcode),
            telephoneNumber
        )

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

    private fun CreateAppointment.withManager(pm: PersonManager) = uk.gov.justice.digital.hmpps.entity.Appointment(
        pm.person,
        appointmentTypeRepository.getByCode(type.code),
        start.toLocalDate(),
        start,
        end,
        notes,
        pm.probationAreaId,
        pm.team,
        pm.staff,
        urn
    )

    private fun uk.gov.justice.digital.hmpps.entity.Appointment.alert(personManager: PersonManager) = Alert(
        id, type.id, person.id, personManager.team.id, personManager.staff.id, personManager.id
    )
}
