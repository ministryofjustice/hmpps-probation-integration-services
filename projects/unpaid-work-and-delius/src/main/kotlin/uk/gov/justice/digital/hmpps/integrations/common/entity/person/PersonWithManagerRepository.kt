package uk.gov.justice.digital.hmpps.integrations.common.entity.person

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface PersonWithManagerRepository : JpaRepository<PersonWithManager, Long> {
    @EntityGraph(attributePaths = ["managers"])
    fun findByCrnAndSoftDeletedIsFalse(crn: String): PersonWithManager?
}
