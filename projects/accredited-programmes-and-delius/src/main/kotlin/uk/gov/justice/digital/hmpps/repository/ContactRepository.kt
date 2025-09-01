package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.contact.ContactOutcome
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
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

    @Modifying
    @Query("update Contact c set c.softDeleted = true WHERE c.externalReference in :externalReferences")
    fun softDeleteByExternalReferenceIn(externalReferences: Set<String>)
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