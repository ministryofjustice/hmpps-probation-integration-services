package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import java.time.LocalDate

interface ContactRepository : JpaRepository<Contact, Long> {
    @Query(
        """
        select c from Contact c
        where c.type.code in ('${ContactType.IAPS_APPOINTMENT}')
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
}
