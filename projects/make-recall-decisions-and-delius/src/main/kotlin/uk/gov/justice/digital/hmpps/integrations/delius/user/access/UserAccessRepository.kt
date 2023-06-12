package uk.gov.justice.digital.hmpps.integrations.delius.user.access

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.api.model.UserAccess
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.entity.Person

interface UserAccessRepository : JpaRepository<Person, Long> {
    @Query(
        """
        select new uk.gov.justice.digital.hmpps.api.model.UserAccess(
            (select p.exclusionMessage from UserAccessPerson p where p.crn = :crn
                    and exists (select e from Exclusion e where upper(e.user.username) = upper(:username) and e.person.id = p.id)),
            (select p.restrictionMessage from UserAccessPerson p where p.crn = :crn
                    and not exists (select r from Restriction r where upper(r.user.username) = upper(:username) and r.person.id = p.id)
                    and exists (select r from Restriction r where r.person.id = p.id))
        )
        """
    )
    fun findByUsernameAndCrn(username: String, crn: String): UserAccess
    fun existsByCrn(crn: String): Boolean
}
