package uk.gov.justice.digital.hmpps.appointments.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.AppointmentOutcome
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.CodedReferenceData
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.EnforcementAction
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.OfficeLocation
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Staff
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Team
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Type
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy

internal object AppointmentReferenceDataRepositories {
    interface ReferenceDataRepository<T : CodedReferenceData> {
        fun findByCode(code: String): T?
        fun findAllByCodeIn(code: Set<String>): List<T>
    }

    inline fun <reified T : CodedReferenceData> ReferenceDataRepository<T>.getByCode(code: String) =
        findByCode(code).orNotFoundBy("code", code)

    inline fun <reified T : CodedReferenceData> ReferenceDataRepository<T>.getAllByCodeIn(codes: List<String>) =
        codes.toSet().let { codes -> findAllByCodeIn(codes).associateBy { it.code }.reportMissing(codes) }

    interface TypeRepository : ReferenceDataRepository<Type>, JpaRepository<Type, Long>

    interface OutcomeRepository : ReferenceDataRepository<AppointmentOutcome>, JpaRepository<AppointmentOutcome, Long>

    interface LocationRepository : ReferenceDataRepository<OfficeLocation>, JpaRepository<OfficeLocation, Long>

    interface EnforcementActionRepository : ReferenceDataRepository<EnforcementAction>,
        JpaRepository<EnforcementAction, Long> {
        @EntityGraph(attributePaths = ["type"])
        override fun findByCode(code: String): EnforcementAction

        @EntityGraph(attributePaths = ["type"])
        override fun findAllByCodeIn(code: Set<String>): List<EnforcementAction>
    }

    interface StaffRepository : ReferenceDataRepository<Staff>, JpaRepository<Staff, Long>

    interface TeamRepository : ReferenceDataRepository<Team>, JpaRepository<Team, Long> {
        @EntityGraph(attributePaths = ["provider"])
        override fun findByCode(code: String): Team

        @EntityGraph(attributePaths = ["provider"])
        override fun findAllByCodeIn(code: Set<String>): List<Team>
    }

    inline fun <reified T> Map<String, T>.reportMissing(codes: Set<String>) = also {
        val missing = codes - keys
        require(missing.isEmpty()) { "Invalid ${T::class.simpleName} codes: $missing" }
    }

    inline fun <reified T> Map<Long, T>.reportMissingIds(ids: Set<Long>) = also {
        val missing = ids - keys
        require(missing.isEmpty()) { "Invalid ${T::class.simpleName} IDs: $missing" }
    }
}