package uk.gov.justice.digital.hmpps.integrations.common.entity.person

import org.springframework.data.jpa.repository.JpaRepository

interface PersonWithManagerRepository : JpaRepository<PersonWithManager, Long> {
    fun findByCrnAndSoftDeletedIsFalse(crn: String): PersonWithManager?
}
