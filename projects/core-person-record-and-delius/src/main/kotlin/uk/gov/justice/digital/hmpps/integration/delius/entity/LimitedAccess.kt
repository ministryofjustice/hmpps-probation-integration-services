package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

@Entity
@Immutable
@SQLRestriction("exclusion_end_time is null or exclusion_end_time > current_date")
class Exclusion(
    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: LimitedAccessUser,

    @Column(name = "exclusion_end_time")
    val endDate: LocalDateTime?,

    @Id
    @Column(name = "exclusion_id")
    val id: Long
)

@Entity
@Immutable
@SQLRestriction("restriction_end_time is null or restriction_end_time > current_date")
class Restriction(
    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: LimitedAccessUser,

    @Column(name = "restriction_end_time")
    val endDate: LocalDateTime?,

    @Id
    @Column(name = "restriction_id")
    val id: Long
)

@Entity
@Immutable
@Table(name = "user_")
class LimitedAccessUser(
    @Column(name = "distinguished_name")
    val username: String,

    @Id
    @Column(name = "user_id")
    val id: Long
)

interface ExclusionRepository : JpaRepository<Exclusion, Long> {
    fun findByPersonId(personId: Long): List<Exclusion>
}

interface RestrictionRepository : JpaRepository<Restriction, Long> {
    fun findByPersonId(personId: Long): List<Restriction>
}
