package uk.gov.justice.digital.hmpps.integrations.delius.user.access

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.user.AuditUser

interface UserAccessRepository : JpaRepository<AuditUser, Long> {
    @Query(
        """
        select new uk.gov.justice.digital.hmpps.integrations.delius.user.access.UserPersonAccess(p.crn, p.exclusionMessage, '')
        from Person p where p.crn in :crns
        and exists (select e from Exclusion e where upper(e.user.username) = upper(:username) and e.person.id = p.id and (e.end is null or e.end > current_date ))
        union
        select new uk.gov.justice.digital.hmpps.integrations.delius.user.access.UserPersonAccess(p.crn, '', p.restrictionMessage)
        from Person p where p.crn in :crns
        and exists (select r from Restriction r where r.person.id = p.id and (r.end is null or r.end > current_date ))
        and not exists (select r from Restriction r where upper(r.user.username) = upper(:username) and r.person.id = p.id and (r.end is null or r.end > current_date ))
    """
    )
    fun getAccessFor(username: String, crns: List<String>): List<UserPersonAccess>
}

data class UserPersonAccess(
    val crn: String,
    val exclusionMessage: String?,
    val restrictionMessage: String?
) {
    fun isExcluded(): Boolean = !exclusionMessage.isNullOrBlank()
    fun isRestricted(): Boolean = !restrictionMessage.isNullOrBlank()
}
