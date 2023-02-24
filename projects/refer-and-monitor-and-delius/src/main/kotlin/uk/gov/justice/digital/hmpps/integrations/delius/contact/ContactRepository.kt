package uk.gov.justice.digital.hmpps.integrations.delius.contact

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.AppointmentOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType.Code.CRSAPT
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType.Code.CRSSAA
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Enforcement
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.EnforcementAction
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

interface ContactRepository : JpaRepository<Contact, Long> {
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
        date: ZonedDateTime = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
    )
}

fun ContactRepository.getAppointmentById(id: Long): Contact =
    findById(id).orElseThrow { NotFoundException("Appointment", "id", id) }

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

fun ContactTypeRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("ContactType", "code", code)

interface ContactOutcomeRepository : JpaRepository<AppointmentOutcome, Long> {
    fun findByCode(code: String): AppointmentOutcome?
}

fun ContactOutcomeRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("ContactOutcome", "code", code)

interface EnforcementRepository : JpaRepository<Enforcement, Long>
interface EnforcementActionRepository : JpaRepository<EnforcementAction, Long> {
    fun findByCode(code: String): EnforcementAction?
}

fun EnforcementActionRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("EnforcementAction", "code", code)
