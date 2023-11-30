package uk.gov.justice.digital.hmpps.integrations.delius.manager.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Immutable
@Entity
@Table(name = "offender_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class CommunityManager(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "offender_manager_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class Person(

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "noms_number", columnDefinition = "char(7)")
    val nomsId: String? = null,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "offender_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "team")
class Team(

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    val description: String,

    @Column(name = "email_address")
    val email: String?,
    val telephone: String?,

    @Id
    @Column(name = "team_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "staff")
class Staff(

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    val forename: String,
    val surname: String,

    @OneToOne(mappedBy = "staff")
    val user: StaffUser?,

    @Id
    @Column(name = "staff_id")
    val id: Long
)

@Entity
@Immutable
@Table(name = "user_")
class StaffUser(

    @Column(name = "distinguished_name")
    val username: String,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff?,

    @Id
    @Column(name = "user_id")
    val id: Long
) {
    @Transient
    var email: String? = null

    @Transient
    var telephone: String? = null
}

interface CommunityManagerRepository : JpaRepository<CommunityManager, Long> {
    fun findByPersonNomsId(nomsId: String): CommunityManager?
}

fun CommunityManagerRepository.getByNomsId(nomsId: String): CommunityManager =
    findByPersonNomsId(nomsId) ?: throw NotFoundException("Person", "nomsId", nomsId)
