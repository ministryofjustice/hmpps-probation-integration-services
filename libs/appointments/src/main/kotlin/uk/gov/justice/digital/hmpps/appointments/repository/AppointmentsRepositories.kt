package uk.gov.justice.digital.hmpps.appointments.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEnforcement
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.AppointmentType
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.EnforcementAction
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Staff
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Team
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentOutcome
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy

object AppointmentsRepositories {
    interface PersonRepository : JpaRepository<AppointmentEntities.Person, Long> {
        fun findByCrn(crn: String): AppointmentEntities.Person?
        fun getPerson(crn: String): AppointmentEntities.Person =
            findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
    }

    interface EnforcementRepository : JpaRepository<AppointmentEnforcement, Long> {
        fun findByContactId(appointmentId: Long): AppointmentEnforcement?
    }

    interface ReferenceDataRepository<T : AppointmentEntities.CodedReferenceData> {
        fun findByCode(code: String): T?
        fun findAllByCodeIn(code: Set<String>): List<T>
    }

    inline fun <reified T : AppointmentEntities.CodedReferenceData> ReferenceDataRepository<T>.getByCode(code: String) =
        findByCode(code).orNotFoundBy("code", code)

    inline fun <reified T : AppointmentEntities.CodedReferenceData> ReferenceDataRepository<T>.getAllByCodeIn(codes: List<String>) =
        codes.toSet().let { codes -> findAllByCodeIn(codes).associateBy { it.code }.reportMissing(codes) }

    interface TypeRepository : ReferenceDataRepository<AppointmentType>, JpaRepository<AppointmentType, Long>

    interface OutcomeRepository : ReferenceDataRepository<AppointmentOutcome>, JpaRepository<AppointmentOutcome, Long>

    interface LocationRepository : ReferenceDataRepository<AppointmentEntities.OfficeLocation>,
        JpaRepository<AppointmentEntities.OfficeLocation, Long>

    interface EnforcementActionRepository : ReferenceDataRepository<EnforcementAction>,
        JpaRepository<EnforcementAction, Long>

    interface StaffRepository : ReferenceDataRepository<Staff>, JpaRepository<Staff, Long>

    interface TeamRepository : ReferenceDataRepository<Team>, JpaRepository<Team, Long>

    inline fun <reified T> Map<String, T>.reportMissing(codes: Set<String>) = also {
        val missing = codes - keys
        require(missing.isEmpty()) { "Invalid ${T::class.simpleName} codes: $missing" }
    }

    inline fun <reified T> Map<Long, T>.reportMissingIds(ids: Set<Long>) = also {
        val missing = ids - keys
        require(missing.isEmpty()) { "Invalid ${T::class.simpleName} IDs: $missing" }
    }
}