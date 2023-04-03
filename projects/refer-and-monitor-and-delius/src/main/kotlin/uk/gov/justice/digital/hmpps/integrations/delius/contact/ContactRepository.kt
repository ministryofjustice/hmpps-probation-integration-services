package uk.gov.justice.digital.hmpps.integrations.delius.contact

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType.Code.CRSAPT
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType.Code.CRSSAA
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Enforcement
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.EnforcementAction
import java.time.LocalDate

interface ContactRepository : JpaRepository<Contact, Long> {

    @Query(
        """
        select count(distinct c.date) 
        from Contact c 
        where c.eventId = :eventId and c.complied = false
        and c.type.nationalStandards = true 
        and (:lastResetDate is null or c.date >= :lastResetDate)
        """
    )
    fun countFailureToComply(eventId: Long, lastResetDate: LocalDate?): Long

    @Query(
        """
            select count(distinct c.date) 
            from Contact c 
            join Nsi nsi on nsi.id = c.nsiId
            join Requirement rq on rq.id = nsi.requirementId
            where c.rarActivity = true and c.softDeleted = false
            and (c.attended is null or c.attended = true) 
            and nsi.id = :nsiId 
            and rq.mainCategory.code = 'F'
        """
    )
    fun countNsiRar(nsiId: Long): Long

    @Modifying
    @Transactional
    @Query(
        """
        delete from Contact c
        where c.nsiId = :nsiId
        and c.type.id in (select ct.id from ContactType ct where ct.code in :contactTypes)
        and c.outcome is null
        and c.date >= :date
    """
    )
    fun deleteFutureAppointmentsForNsi(
        nsiId: Long,
        contactTypes: List<String> = listOf(CRSAPT.value, CRSSAA.value),
        date: LocalDate = LocalDate.now()
    )
}

fun ContactRepository.getAppointmentById(id: Long): Contact =
    findById(id).orElseThrow { NotFoundException("Appointment", "id", id) }

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
