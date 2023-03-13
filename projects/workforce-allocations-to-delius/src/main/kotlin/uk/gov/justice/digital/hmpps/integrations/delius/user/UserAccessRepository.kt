package uk.gov.justice.digital.hmpps.integrations.delius.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.user.User
import java.time.LocalDateTime

interface UserAccessRepository : JpaRepository<User, Long> {
    @Query(
        """
        select new uk.gov.justice.digital.hmpps.integrations.delius.user.UserPersonAccess(p.crn, p.exclusionMessage, '') from Exclusion e
        join e.user u
        join e.person p
        where u.username = :username and p.crn in :crns
        and (e.end is null or e.end > current_date)
        union
        select new uk.gov.justice.digital.hmpps.integrations.delius.user.UserPersonAccess(p.crn, '', p.restrictionMessage) from Restriction r
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

@Immutable
@Entity
class Exclusion(
    @Id
    @Column(name = "exclusion_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,

    @Column(name = "exclusion_end_time")
    val end: LocalDateTime? = null
)

@Immutable
@Entity
class Restriction(
    @Id
    @Column(name = "restriction_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,

    @Column(name = "restriction_end_time")
    val end: LocalDateTime? = null
)
