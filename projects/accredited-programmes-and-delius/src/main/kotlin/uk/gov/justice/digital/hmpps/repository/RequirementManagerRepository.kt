package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.sentence.component.manager.RequirementManager

interface RequirementManagerRepository : JpaRepository<RequirementManager, Long> {
    @EntityGraph(
        attributePaths = [
            "staff.user",
            "team.localAdminUnit.probationDeliveryUnit",
            "team.officeLocations"
        ]
    )
    fun findByRequirementId(requirementId: Long): RequirementManager?
}
