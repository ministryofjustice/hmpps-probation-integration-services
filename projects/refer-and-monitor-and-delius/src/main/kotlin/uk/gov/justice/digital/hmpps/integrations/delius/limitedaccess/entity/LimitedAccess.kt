package uk.gov.justice.digital.hmpps.integrations.delius.limitedaccess.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.user.AuditUser
import java.time.LocalDateTime

@Immutable
@Entity
class Exclusion(
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: AuditUser,

    @Column(name = "exclusion_end_time")
    val end: LocalDateTime?,

    @Id
    @Column(name = "exclusion_id")
    val id: Long
)

@Immutable
@Entity
class Restriction(
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: AuditUser,

    @Column(name = "restriction_end_time")
    val end: LocalDateTime?,

    @Id
    @Column(name = "restriction_id")
    val id: Long
)

interface UserAccessRepository : JpaRepository<AuditUser, Long> {
    @Query(
        """
        select p.crn as crn, '' as exclusionMessage, p.restrictionMessage as restrictionMessage
        from Person p where p.crn in :crns
        and exists (select r from Restriction r where r.person.id = p.id and (r.end is null or r.end > current_date ))
        and not exists (select r from Restriction r where upper(r.user.username) = upper(:username) and r.person.id = p.id and (r.end is null or r.end > current_date ))
        union
        select p.crn as crn, p.exclusionMessage as exclusionMessage, '' as restrictionMessage
        from Person p where p.crn in :crns
        and exists (select e from Exclusion e where upper(e.user.username) = upper(:username) and e.person.id = p.id and (e.end is null or e.end > current_date ))
    """
    )
    fun getAccessFor(username: String, crns: List<String>): List<PersonAccess>
}

interface PersonAccess {
    val crn: String
    val exclusionMessage: String?
    val restrictionMessage: String?
}

fun PersonAccess.isExcluded() = !exclusionMessage.isNullOrBlank()
fun PersonAccess.isRestricted() = !restrictionMessage.isNullOrBlank()
