package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import kotlin.jvm.Transient

@Immutable
@Entity
@Table(name = "staff")
class Staff(

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    val forename: String,
    val surname: String,

    @Column(name = "forename2")
    val middleName: String?,

    @OneToOne(mappedBy = "staff")
    val user: StaffUser?,

    @ManyToMany
    @JoinTable(
        name = "staff_team",
        joinColumns = [JoinColumn(name = "staff_id")],
        inverseJoinColumns = [JoinColumn(name = "team_id")]
    )
    val teams: Set<Team>,

    @Id
    @Column(name = "staff_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "user_")
class StaffUser(

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff?,

    @Column(name = "distinguished_name")
    val username: String,

    @Id
    @Column(name = "user_id")
    val id: Long
) {
    @Transient
    var telephone: String? = null
}

interface StaffUserRepository : JpaRepository<StaffUser, Long> {
    @Query(
        """
        select su from StaffUser su
        left join fetch su.staff s
        left join fetch s.teams t 
        left join fetch t.addresses 
        where upper(su.username) = upper(:username)
    """
    )
    fun findByUsername(username: String): StaffUser?
}