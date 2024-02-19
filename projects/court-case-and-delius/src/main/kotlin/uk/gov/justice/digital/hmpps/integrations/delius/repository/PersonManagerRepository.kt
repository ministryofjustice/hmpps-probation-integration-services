package uk.gov.justice.digital.hmpps.integrations.delius.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonManager

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    @Query(
        """
        select pm from PersonManager pm 
        where pm.person.id = :personId
        and pm.active = true
        and pm.softDeleted = false
        """
    )
    fun findActiveManager(personId: Long): PersonManager?

    @Query(
        """
        select pm from PersonManager pm 
        where pm.person.crn = :crn
        and pm.active = true
        and pm.softDeleted = false
        """
    )
    fun findActiveManager(crn: String): PersonManager
}
