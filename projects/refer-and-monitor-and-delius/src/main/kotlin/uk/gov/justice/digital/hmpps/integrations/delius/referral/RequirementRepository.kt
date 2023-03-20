package uk.gov.justice.digital.hmpps.integrations.delius.referral

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Requirement

interface RequirementRepository : JpaRepository<Requirement, Long> {
    @Query(
        """
            select r from Requirement r 
            where r.personId = :personId
            and r.disposal.eventId = :eventId
            and r.mainCategory.code = :type
        """
    )
    fun findForPersonAndEvent(
        personId: Long,
        eventId: Long,
        type: String,
        pageable: Pageable = Pageable.ofSize(1)
    ): List<Requirement>
}
