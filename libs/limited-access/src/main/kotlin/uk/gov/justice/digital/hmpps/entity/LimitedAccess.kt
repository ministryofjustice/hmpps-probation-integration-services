package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.OffsetDateTime
import java.time.ZonedDateTime

@Immutable
@Entity
class Exclusion(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: LimitedAccessPerson,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: LimitedAccessUser,

    @Column(name = "exclusion_date")
    val start: ZonedDateTime,

    @Column(name = "exclusion_end_time")
    val end: ZonedDateTime?,

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

    @Column(name = "restriction_time")
    val start: ZonedDateTime,

    @Column(name = "restriction_end_time")
    val end: ZonedDateTime?,

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
        select offender.crn as crn,
               user_.distinguished_name as username,
               l.type as type,
               offender.exclusion_message as exclusionMessage,
               offender.restriction_message as restrictionMessage,
               l.start_date as startDate,
               l.end_date as endDate,
               l.start_date as createdDateTime,
               l.end_date as lastUpdatedDateTime
        from ( ( select offender_id,
                        user_id,
                        'Restriction' as type,
                        cast(restriction_time as timestamp with time zone) as start_date,
                        cast(restriction_end_time as timestamp with time zone) as end_date
                 from restriction )
               union all
               ( select offender_id,
                        user_id,
                        'Exclusion' as type,
                        cast(exclusion_date as timestamp with time zone) as start_date,
                        cast(exclusion_end_time as timestamp with time zone) as end_date
                 from exclusion ) ) l
        join offender on offender.offender_id = l.offender_id
        join user_ on user_.user_id = l.user_id
        order by offender.crn, l.type, user_.distinguished_name
    """,
        countQuery = """
        select count(1)
        from ( ( select offender_id,
                        user_id,
                        'Restriction' as type,
                        cast(restriction_time as timestamp with time zone) as start_date,
                        cast(restriction_end_time as timestamp with time zone) as end_date
                 from restriction )
               union all
               ( select offender_id,
                        user_id,
                        'Exclusion' as type,
                        cast(exclusion_date as timestamp with time zone) as start_date,
                        cast(exclusion_end_time as timestamp with time zone) as end_date
                 from exclusion ) ) l
        join offender on offender.offender_id = l.offender_id
        join user_ on user_.user_id = l.user_id
    """,
        nativeQuery = true
    )
    fun getAll(page: Pageable): Page<LimitedAccessRow>

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
        select e.user.username as username, e.start as since, e.end as until
        from Exclusion e
        where e.person.crn = :crn
        and (e.end is null or e.end > current_date)
        """
    )
    fun getExclusionsForCrn(crn: String): List<ExclusionDetail>

    @Query(
        """
        select r.user.username as username, r.start as since, r.end as until
        from Restriction r
        where r.person.crn = :crn
        and (r.end is null or r.end > current_date)
        """
    )
    fun getRestrictionsForCrn(crn: String): List<RestrictionDetail>

    @Query("select p from LimitedAccessPerson p where p.crn = :crn")
    fun findLimitedAccessPersonByCrn(crn: String): LimitedAccessPerson?

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

interface RestrictionDetail {
    val username: String
    val since: ZonedDateTime
    val until: ZonedDateTime?
}

interface ExclusionDetail {
    val username: String
    val since: ZonedDateTime
    val until: ZonedDateTime?
}

interface LimitedAccessRow {
    val crn: String
    val username: String
    val type: String
    val exclusionMessage: String?
    val restrictionMessage: String?
    val startDate: OffsetDateTime
    val endDate: OffsetDateTime?
    val createdDateTime: OffsetDateTime
    val lastUpdatedDateTime: OffsetDateTime?
}

data class LimitedAccessDetail(
    val crn: String,
    val username: String,
    val type: String,
    val exclusionMessage: String?,
    val restrictionMessage: String?,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime?,
    val createdDateTime: ZonedDateTime,
    val lastUpdatedDateTime: ZonedDateTime?,
)

