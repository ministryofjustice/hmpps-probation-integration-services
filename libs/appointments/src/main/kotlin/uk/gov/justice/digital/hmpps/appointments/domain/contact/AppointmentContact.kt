package uk.gov.justice.digital.hmpps.appointments.domain.contact

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentContact
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME

interface AppointmentRepository : JpaRepository<AppointmentContact, Long> {
    @Query(
        """
            select count(app.id) from Contact app 
            where app.event.id = :eventId and app.type.code = :contactCode 
            and app.outcome is null and (:breachEnd is null or app.date >= :breachEnd)
        """
    )
    fun countEnforcementUnderReview(
        @Param("eventId") eventId: Long,
        @Param("contactCode") contactCode: String,
        @Param("breachEnd") breachEnd: LocalDate?
    ): Long

    @Query(
        """
        select count(distinct app.date) 
        from Contact app 
        where app.event.id = :eventId and app.complied = false
        and app.type.nationalStandards = true 
        and (:lastResetDate is null or app.date >= :lastResetDate)
        """
    )
    fun countFailureToComply(eventId: Long, lastResetDate: LocalDate?): Long

    @Query(
        """
            select count(c.contact_id)
            from contact c
            join r_contact_type ct on c.contact_type_id = ct.contact_type_id
            join offender p on p.offender_id = c.offender_id
            where p.crn = :personCrn and ct.attendance_contact = 'Y'
            and c.external_reference <> :externalReference
            and to_char(c.contact_date, 'YYYY-MM-DD') = :date
            and to_char(c.contact_start_time, 'HH24:MI') < :endTime 
            and to_char(c.contact_end_time, 'HH24:MI') > :startTime
            and c.soft_deleted = 0 and c.contact_outcome_type_id is null
        """,
        nativeQuery = true
    )
    fun getClashCount(
        personCrn: String,
        externalReference: String,
        date: String,
        startTime: String,
        endTime: String,
    ): Int

    fun findByExternalReference(externalReference: String): AppointmentContact?
    fun findByExternalReferenceIn(externalReference: List<String>): List<AppointmentContact>
}

fun AppointmentRepository.appointmentClashes(
    personCrn: String,
    externalReference: String,
    date: LocalDate,
    startTime: ZonedDateTime,
    endTime: ZonedDateTime,
): Boolean = getClashCount(
    personCrn,
    externalReference,
    date.format(ISO_LOCAL_DATE),
    startTime.format(ISO_LOCAL_TIME.withZone(EuropeLondon)),
    endTime.format(ISO_LOCAL_TIME.withZone(EuropeLondon)),
) > 0

fun AppointmentRepository.getAppointment(externalReference: String): AppointmentContact =
    findByExternalReference(externalReference)
        ?: throw NotFoundException("Appointment", "externalReference", externalReference)