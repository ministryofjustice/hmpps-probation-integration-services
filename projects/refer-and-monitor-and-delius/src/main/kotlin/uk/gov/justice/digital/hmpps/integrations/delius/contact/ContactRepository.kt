package uk.gov.justice.digital.hmpps.integrations.delius.contact

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType.Code.CRSAPT
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType.Code.CRSSAA
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Enforcement
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.EnforcementAction
import uk.gov.justice.digital.hmpps.integrations.delius.projections.ContactNotFoundReason
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME

interface ContactRepository : JpaRepository<Contact, Long> {
    @Query(
        """
        select count(distinct c.date) 
        from Contact c 
        where c.eventId = :eventId and c.complied = false
        and c.type.nationalStandards = true 
        and (:lastResetDate is null or c.date >= :lastResetDate)
        """,
    )
    fun countFailureToComply(
        eventId: Long,
        lastResetDate: LocalDate?,
    ): Long

    @Query(
        """
            select count(c.id) from Contact c 
            where c.eventId = :eventId and c.type.code = :contactCode 
            and c.outcome is null and (:breachEnd is null or c.date >= :breachEnd)
        """,
    )
    fun countEnforcementUnderReview(
        @Param("eventId") eventId: Long,
        @Param("contactCode") contactCode: String,
        @Param("breachEnd") breachEnd: LocalDate?,
    ): Long

    @Query(
        """
            select count(distinct c.date) 
            from Contact c 
            join Nsi nsi on nsi.id = c.nsiId
            join Requirement rq on rq.id = nsi.requirementId
            where c.rarActivity = true and c.softDeleted = false
            and (c.attended is null or c.attended = true) 
            and (c.complied is null or c.complied = true)
            and nsi.id = :nsiId 
            and rq.mainCategory.code = 'F'
        """,
    )
    fun countNsiRar(nsiId: Long): Long

    @Modifying
    @Transactional
    @Query(
        """
        update Contact c
        set c.outcome = :appointmentWithdrawn,
        c.attended = false,
        c.complied = true
        where c.nsiId = :nsiId
        and c.type.id in (select ct.id from ContactType ct where ct.code in :contactTypes)
        and c.outcome is null
        and c.date >= :date
    """,
    )
    fun withdrawFutureAppointments(
        nsiId: Long,
        appointmentWithdrawn: ContactOutcome,
        contactTypes: List<String> = listOf(CRSAPT.value, CRSSAA.value),
        date: LocalDate = LocalDate.now(),
    )

    fun findByPersonCrnAndExternalReference(
        crn: String,
        externalReference: String,
    ): Contact?

    @Query(
        """
            select count(c.contact_id)
            from contact c
            join r_contact_type ct on c.contact_type_id = ct.contact_type_id
            where c.offender_id = :personId and ct.attendance_contact = 'Y'
            and (c.external_reference is null or c.external_reference <> :externalReference)
            and (:previousExternalReference is null or c.external_reference <> :previousExternalReference)
            and to_char(c.contact_date, 'YYYY-MM-DD') = :date
            and to_char(c.contact_start_time, 'HH24:MI') < :endTime 
            and to_char(c.contact_end_time, 'HH24:MI') > :startTime
            and c.soft_deleted = 0 and c.contact_outcome_type_id is null
        """,
        nativeQuery = true,
    )
    fun getClashCount(
        personId: Long,
        externalReference: String,
        date: String,
        startTime: String,
        endTime: String,
        previousExternalReference: String?,
    ): Int

    @Query(
        """
            select c from Contact c 
            where c.nsiId = :nsiId
            and c.type.code = :contactType
            and c.date = :date
        """,
    )
    fun findNotificationContact(
        nsiId: Long,
        contactType: String,
        date: LocalDate,
    ): List<Contact>

    @Query(
        """
            select 
                (select contact.soft_deleted 
                    from contact 
                    join offender contact_offender on contact_offender.offender_id = contact.offender_id 
                    where (contact_offender.crn = :crn and contact.external_reference = :contactExternalReference) 
                    or contact_id = :contactId
                ) as softDeleted,
                nsi.soft_deleted as nsiSoftDeleted,
                nsi.active_flag as nsiActive,
                last_updated_by.distinguished_name as nsiLastUpdatedBy
            from nsi
            join offender nsi_offender on nsi_offender.offender_id = nsi.offender_id 
            left join user_ last_updated_by on last_updated_by.user_id = nsi.last_updated_user_id
            where nsi_offender.crn = :crn and nsi.external_reference = :nsiExternalReference
        """,
        nativeQuery = true,
    )
    fun getNotFoundReason(
        crn: String,
        nsiExternalReference: String,
        contactExternalReference: String,
        contactId: Long,
    ): ContactNotFoundReason?
}

fun ContactRepository.appointmentClashes(
    personId: Long,
    externalReference: String,
    date: LocalDate,
    startTime: ZonedDateTime,
    endTime: ZonedDateTime,
    previousExternalReference: String?,
): Boolean =
    getClashCount(
        personId,
        externalReference,
        date.format(ISO_LOCAL_DATE),
        startTime.format(ISO_LOCAL_TIME.withZone(ZoneId.systemDefault())),
        endTime.format(ISO_LOCAL_TIME.withZone(ZoneId.systemDefault())),
        previousExternalReference,
    ) > 0

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

fun ContactTypeRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("ContactType", "code", code)

interface ContactOutcomeRepository : JpaRepository<ContactOutcome, Long> {
    fun findByCode(code: String): ContactOutcome?
}

fun ContactOutcomeRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("ContactOutcome", "code", code)

interface EnforcementRepository : JpaRepository<Enforcement, Long> {
    fun findByContactId(contactId: Long): Enforcement?
}

interface EnforcementActionRepository : JpaRepository<EnforcementAction, Long> {
    fun findByCode(code: String): EnforcementAction?
}

fun EnforcementActionRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("EnforcementAction", "code", code)
