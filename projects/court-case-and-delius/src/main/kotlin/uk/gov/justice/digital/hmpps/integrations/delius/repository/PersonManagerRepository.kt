package uk.gov.justice.digital.hmpps.integrations.delius.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonManager

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    @Query(
        """
        select pm from PersonManager pm 
        where pm.personId = :personId
        and pm.active = true
        and pm.softDeleted = false
        """
    )
    fun findActiveManager(personId: Long): PersonManager?
}
