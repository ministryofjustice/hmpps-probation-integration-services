package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
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
        where c.type.code in ('${ContactType.APPOINTMENT}', '${ContactType.IAPS_APPOINTMENT}')
        and (c.requirement.id in :requirementIds or c.licenceCondition.id in :licenceConditionIds)
        and (:fromDate is null or c.date >= :fromDate)
        and (:toDate is null or c.date <= :toDate)
        """
    )
    @EntityGraph(
        attributePaths = [
            "person",
            "type",
            "outcome",
            "location",
            "provider",
            "event.person.gender.dataset",
            "event.person.ethnicity.dataset",
            "event.person.manager",
            "event.disposal.custody",
            "event.disposal.type",
            "event.twoThirdsContacts",
            "requirement.mainCategory",
            "requirement.subCategory",
            "requirement.disposal.type",
            "requirement.manager",
            "requirement.terminationReason",
            "requirement.disposal.lengthUnits",
            "requirement.disposal.event.person.gender.dataset",
            "requirement.disposal.event.person.ethnicity.dataset",
            "requirement.disposal.event.person.manager.staff.user",
            "requirement.disposal.event.person.manager.team.localAdminUnit.probationDeliveryUnit",
            "requirement.disposal.event.person.manager.team.provider",
            "requirement.disposal.custody",
            "licenceCondition.mainCategory",
            "licenceCondition.subCategory",
            "licenceCondition.disposal.type",
            "licenceCondition.manager",
            "licenceCondition.terminationReason",
            "licenceCondition.disposal.lengthUnits",
            "licenceCondition.disposal.event.person.gender.dataset",
            "licenceCondition.disposal.event.person.ethnicity.dataset",
            "licenceCondition.disposal.event.person.manager.staff.user",
            "licenceCondition.disposal.event.person.manager.team.localAdminUnit.probationDeliveryUnit",
            "licenceCondition.disposal.event.person.manager.team.provider",
            "licenceCondition.disposal.custody",
            "team.localAdminUnit.probationDeliveryUnit",
            "team.provider",
            "createdByUser.staff",
        ]
    )
    fun findAllByComponentIdInDateRange(
        requirementIds: List<Long>,
        licenceConditionIds: List<Long>,
        fromDate: LocalDate?,
        toDate: LocalDate?,
    ): List<Contact>

    fun findByExternalReference(externalReference: String): Contact?

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

