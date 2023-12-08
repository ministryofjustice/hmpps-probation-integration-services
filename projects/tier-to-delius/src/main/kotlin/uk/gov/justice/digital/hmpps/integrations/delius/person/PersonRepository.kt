package uk.gov.justice.digital.hmpps.integrations.delius.person

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PersonRepository : JpaRepository<Person, Long> {
    fun findByCrnAndSoftDeletedIsFalse(crn: String): Person?

    @Query(
        """
        select distinct p.crn from EventEntity e join e.person p 
        where e.softDeleted = false and e.active = true
        and p.softDeleted = false
        """,
    )
    fun findAllCrns(): List<String>
}
