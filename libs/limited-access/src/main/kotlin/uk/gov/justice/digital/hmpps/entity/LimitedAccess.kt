package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

@Immutable
@Entity
class Exclusion(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: LimitedAccessPerson,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: LimitedAccessUser,

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
    val person: LimitedAccessPerson,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: LimitedAccessUser,

    @Column(name = "restriction_end_time")
    val end: LocalDateTime?,

    @Id
    @Column(name = "restriction_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "offender")
class LimitedAccessPerson(

    @Column(columnDefinition = "char(7)")
    val crn: String,

    val exclusionMessage: String?,
    val restrictionMessage: String?,

    @Id
    @Column(name = "offender_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "user_")
class LimitedAccessUser(

    @Column(name = "distinguished_name")
    val username: String,

    @Id
    @Column(name = "user_id")
    val id: Long
)

interface UserAccessRepository : JpaRepository<LimitedAccessUser, Long> {
    @Query("select u from LimitedAccessUser u where upper(u.username) = upper(:username) ")
    fun findByUsername(username: String): LimitedAccessUser?

    @Query(
        """
        select p.crn as crn, 'false' as excluded, '' as exclusionMessage, 'true' as restricted, p.restrictionMessage as restrictionMessage
        from LimitedAccessPerson p where p.crn in :crns
        and exists (select r from Restriction r where r.person.id = p.id and (r.end is null or r.end > current_date ))
        and not exists (select r from Restriction r where upper(r.user.username) = upper(:username) and r.person.id = p.id and (r.end is null or r.end > current_date ))
        union
        select p.crn as crn, 'true' as excluded, p.exclusionMessage as exclusionMessage, 'false' as restricted, '' as restrictionMessage
        from LimitedAccessPerson p where p.crn in :crns
        and exists (select e from Exclusion e where upper(e.user.username) = upper(:username) and e.person.id = p.id and (e.end is null or e.end > current_date ))
    """
    )
    fun getAccessFor(username: String, crns: List<String>): List<PersonAccess>

    @Query(
        """
        select p.crn as crn, 'false' as excluded, '' as exclusionMessage, 'true' as restricted, p.restrictionMessage as restrictionMessage
        from LimitedAccessPerson p where p.crn in :crns
        and exists (select r from Restriction r where r.person.id = p.id and (r.end is null or r.end > current_date ))
        union
        select p.crn as crn, 'true' as excluded, p.exclusionMessage as exclusionMessage, 'false' as restricted, '' as restrictionMessage
        from LimitedAccessPerson p where p.crn in :crns
        and exists (select e from Exclusion e where e.person.id = p.id and (e.end is null or e.end > current_date ))
    """
    )
    fun checkLimitedAccessFor(crns: List<String>): List<PersonAccess>
}

interface PersonAccess {
    val crn: String
    val excluded: Boolean
    val exclusionMessage: String?
    val restricted: Boolean
    val restrictionMessage: String?
}
