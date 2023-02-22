package uk.gov.justice.digital.hmpps.integrations.delius.appointment

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.Appointment
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.AppointmentOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.AppointmentType
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.Enforcement
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.EnforcementAction
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

interface AppointmentRepository : JpaRepository<Appointment, Long> {
    @Modifying
    @Query(
        """
        delete from Appointment a 
        where a.nsiId = :nsiId 
        and a.type.code in :appointmentTypes 
        and a.outcome is null
        and a.date >= :date
    """
    )
    fun deleteFutureAppointmentsForNsi(
        nsiId: Long,
        appointmentTypes: List<String> = listOf(AppointmentType.Code.CRSAPT.name, AppointmentType.Code.CRSSAA.name),
        date: ZonedDateTime = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
    )
}

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
