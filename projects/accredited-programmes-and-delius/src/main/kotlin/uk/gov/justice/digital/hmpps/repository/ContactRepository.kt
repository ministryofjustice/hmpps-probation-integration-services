package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.contact.Contact
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
    @Query("update Contact c set c.softDeleted = true where c.externalReference in :externalReferences")
    fun softDeleteByExternalReferenceIn(externalReferences: Set<String>)

    @[Modifying Query(
        """
        update Contact c 
        set c.softDeleted = true
        where c.requirement.id = :id
        and c.date > :date
        and c.outcome is null
        and c.createdByUser.systemUser = false
        """
    )]
    fun deleteFutureRequirementContacts(id: Long, date: LocalDate)

    @[Modifying Query(
        """
        update Contact c 
        set c.softDeleted = true
        where c.licenceCondition.id = :id
        and c.date > :date
        and c.outcome is null
        and c.createdByUser.systemUser = false
        """
    )]
    fun deleteFutureLicenceConditionContacts(id: Long, date: LocalDate)

    fun findByRequirementIdAndTypeCode(id: Long, typeCode: String): Contact?
    fun findByLicenceConditionIdAndTypeCode(id: Long, typeCode: String): Contact?
}

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

fun ContactTypeRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("ContactType", "code", code)

