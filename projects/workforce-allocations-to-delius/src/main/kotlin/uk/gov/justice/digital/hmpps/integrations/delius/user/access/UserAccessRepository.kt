package uk.gov.justice.digital.hmpps.integrations.delius.user.access

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.user.User

interface UserAccessRepository : JpaRepository<User, Long> {
    @Query(
        """
        select new uk.gov.justice.digital.hmpps.integrations.delius.user.access.UserPersonAccess(p.crn, p.exclusionMessage, '') from Exclusion e
        join e.user u
        join e.person p
        where u.username = :username and p.crn in :crns
        and (e.end is null or e.end > current_date)
        union
        select new uk.gov.justice.digital.hmpps.integrations.delius.user.access.UserPersonAccess(p.crn, '', p.restrictionMessage) from Restriction r
        join r.user u
        join r.person p
        where u.username = :username and p.crn in :crns
        and (r.end is null or r.end > current_date)
    """
    )
    fun getAccessFor(username: String, crns: List<String>): List<UserPersonAccess>
}

data class UserPersonAccess(
    val crn: String,
    val exclusionMessage: String?,
    val restrictionMessage: String?
) {
    fun isExcluded(): Boolean = exclusionMessage != null && exclusionMessage.isNotBlank()
    fun isRestricted(): Boolean = restrictionMessage != null && restrictionMessage.isNotBlank()
}
