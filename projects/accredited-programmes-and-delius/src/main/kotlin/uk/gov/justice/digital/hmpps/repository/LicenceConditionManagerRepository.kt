package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.sentence.component.manager.LicenceConditionManager

interface LicenceConditionManagerRepository : JpaRepository<LicenceConditionManager, Long> {
    @EntityGraph(
        attributePaths = [
            "staff.user",
            "team.localAdminUnit.probationDeliveryUnit",
            "team.officeLocations",
            "licenceCondition.disposal.event.number"
        ]
    )
    fun findByLicenceConditionId(licenceConditionId: Long): LicenceConditionManager?
}
