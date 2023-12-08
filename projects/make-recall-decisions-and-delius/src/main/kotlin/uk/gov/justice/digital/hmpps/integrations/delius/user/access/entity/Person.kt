package uk.gov.justice.digital.hmpps.integrations.delius.user.access.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import java.time.ZonedDateTime

@Immutable
@Table(name = "offender")
@Entity(name = "UserAccessPerson")
@SQLRestriction("soft_deleted = 0")
class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long,
    @Column(columnDefinition = "char(7)")
    val crn: String,
    @OneToMany(mappedBy = "person")
    val exclusions: List<Exclusion>,
    @OneToMany(mappedBy = "person")
    val restrictions: List<Restriction>,
    @Column
    val exclusionMessage: String? = null,
    @Column
    val restrictionMessage: String? = null,
    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,
)

@Immutable
@Entity
@SQLRestriction("exclusion_end_time is null or exclusion_end_time > current_date")
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
    val endDate: ZonedDateTime? = null,
)

@Immutable
@Entity
@SQLRestriction("restriction_end_time is null or restriction_end_time > current_date")
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
    val endDate: ZonedDateTime? = null,
)
