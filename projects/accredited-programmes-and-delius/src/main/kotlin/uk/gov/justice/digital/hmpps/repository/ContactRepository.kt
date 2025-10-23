package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.contact.ContactOutcome
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.entity.sentence.Event
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate

interface ContactRepository : JpaRepository<Contact, Long> {
    @Query(
        """
        select c from Contact c
        where c.type.code = '${ContactType.APPOINTMENT}'
        and (c.requirement.id in :requirementIds or c.licenceCondition.id in :licenceConditionIds)
        and c.date >= :fromDate and c.date <= :toDate
        """
    )
    fun findAllByComponentIdInDateRange(
        requirementIds: List<Long>,
        licenceConditionIds: List<Long>,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<Contact>

    fun findByExternalReference(externalReference: String): Contact?

    fun findByExternalReferenceIn(externalReferences: List<String>): List<Contact>

    @Modifying
    @Query("update Contact c set c.softDeleted = true where c.externalReference in :externalReferences")
    fun softDeleteByExternalReferenceIn(externalReferences: Set<String>)

    @Query(
        """
        select count(distinct c.date)
        from Contact c
        where c.event.id = :eventId
        and c.complied = false
        and c.type.nationalStandards = true
        and (:lastResetDate is null or c.date >= :lastResetDate)
        """
    )
    fun countFailureToComply(
        event: Event,
        eventId: Long = event.id,
        lastResetDate: LocalDate? = listOfNotNull(event.breachEnd, event.disposal?.date).maxOrNull()
    ): Long

    @Query(
        """
        select count(c.id) > 0 from Contact c
        where c.event.id = :eventId
        and c.type.code = :typeCode
        and c.outcome is null
        and (:since is null or c.date >= :since)
        """
    )
    fun enforcementReviewExists(
        eventId: Long,
        since: LocalDate?,
        typeCode: String = ContactType.REVIEW_ENFORCEMENT_STATUS,
    ): Boolean
}

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