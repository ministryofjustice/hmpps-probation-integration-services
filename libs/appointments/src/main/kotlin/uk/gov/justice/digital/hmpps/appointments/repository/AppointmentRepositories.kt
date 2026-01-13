package uk.gov.justice.digital.hmpps.appointments.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.AppointmentContact
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Enforcement
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Person
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Type
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME

object AppointmentRepositories {
    interface AppointmentRepository : JpaRepository<AppointmentContact, Long> {
        @EntityGraph("AppointmentContact.all")
        fun findByExternalReferenceIn(externalReference: List<String>): List<AppointmentContact>

        @Query(
            """
            select count(c.id) > 0 from AppointmentContact c
            where c.event.id = :eventId
            and c.type.code = :typeCode
            and c.outcome is null
            and (:since is null or c.date >= :since)
            """
        )
        fun enforcementReviewExists(
            eventId: Long,
            since: LocalDate?,
            typeCode: String = Type.REVIEW_ENFORCEMENT_STATUS,
        ): Boolean

        @Query(
            """
            select count(distinct c.date)
            from AppointmentContact c
            where c.event.id = :eventId
            and c.complied = false
            and c.type.nationalStandards = true
            and (:lastResetDate is null or c.date >= :lastResetDate)
            """
        )
        fun countFailureToComply(
            event: AppointmentEntities.Event,
            eventId: Long = event.id,
            lastResetDate: LocalDate? = listOfNotNull(event.breachEnd, event.disposal?.date).maxOrNull()
        ): Long

        @Query(
            """
                select c.id
                from AppointmentContact c
                join c.type ct
                join c.person p
                where p.id = :personId
                and c.externalReference <> :externalReference
                and date_trunc('day', c.date) = :date
                and to_char(c.startTime, 'HH24:MI') < to_char(:endTime, 'HH24:MI') 
                and c.endTime is not null 
                and to_char(c.endTime, 'HH24:MI') > to_char(:startTime, 'HH24:MI')
                and ct.attendance = true
                and c.outcome is null
            """
        )
        fun firstConflictingAppointment(
            appointment: AppointmentContact,
            personId: Long = appointment.personId,
            externalReference: String = requireNotNull(appointment.externalReference) { "Reference must be set to check for conflicts" },
            date: LocalDate = appointment.date,
            startTime: ZonedDateTime = appointment.startTime,
            endTime: ZonedDateTime = requireNotNull(appointment.endTime) { "End time must be set to check for conflicts" },
            start: String = startTime.format(ISO_LOCAL_TIME),
            end: String = endTime.format(ISO_LOCAL_TIME),
            pageable: Pageable = Pageable.ofSize(1)
        ): Page<Long>

        fun schedulingConflictExists(appointment: AppointmentContact) =
            firstConflictingAppointment(appointment).hasContent()
    }

    interface PersonRepository : JpaRepository<Person, Long> {
        @Query("select p.id from Person p where p.crn = :crn")
        fun findIdByCrn(crn: String): Long?
        fun getIdByCrn(crn: String): Long =
            findIdByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
    }

    interface EnforcementRepository : JpaRepository<Enforcement, Long> {
        fun existsByContactId(appointmentId: Long): Boolean
    }
}

