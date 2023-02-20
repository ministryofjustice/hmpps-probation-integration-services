package uk.gov.justice.digital.hmpps.integrations.delius.appointment

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.Appointment
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.AppointmentOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.AppointmentType
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.Enforcement
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.EnforcementAction

interface AppointmentRepository : JpaRepository<Appointment, Long>

fun AppointmentRepository.getAppointmentById(id: Long): Appointment =
    findById(id).orElseThrow { NotFoundException("Appointment", "id", id) }

interface AppointmentTypeRepository : JpaRepository<AppointmentType, Long>
interface AppointmentOutcomeRepository : JpaRepository<AppointmentOutcome, Long> {
    fun findByCode(code: String): AppointmentOutcome?
}

fun AppointmentOutcomeRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("AppointmentOutcome", "code", code)

interface EnforcementRepository : JpaRepository<Enforcement, Long>
interface EnforcementActionRepository : JpaRepository<EnforcementAction, Long> {
    fun findByCode(code: String): EnforcementAction?
}

fun EnforcementActionRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("EnforcementAction", "code", code)
