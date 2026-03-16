package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@Table(name = "staff")
class Staff(

    @Id
    @Column(name = "staff_id")
    val id: Long,

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    val forename: String,
    val surname: String,

    @Column(name = "forename2")
    val middleName: String? = null,

    @OneToOne(mappedBy = "staff")
    val user: StaffUser? = null,

    @ManyToMany
    @JoinTable(
        name = "staff_team",
        joinColumns = [JoinColumn(name = "staff_id")],
        inverseJoinColumns = [JoinColumn(name = "team_id")]
    )
    val teams: List<Team>?,
)

@Entity
@Immutable
@Table(name = "user_")
class StaffUser(
    @Id
    @Column(name = "user_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null,

    @Column(name = "distinguished_name")
    val username: String,
)

interface StaffRepository : JpaRepository<Staff, Long> {
    @EntityGraph(attributePaths = ["user", "teams"])
    fun findByUserUsernameIgnoreCase(username: String): Staff?
}