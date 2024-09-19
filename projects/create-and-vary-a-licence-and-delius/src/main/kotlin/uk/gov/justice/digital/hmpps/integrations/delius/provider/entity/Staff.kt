package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
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
    val middleName: String? = null,

    @OneToOne(mappedBy = "staff")
    val user: StaffUser? = null,

    @Id
    @Column(name = "staff_id")
    val id: Long,

    @ManyToMany
    @JoinTable(
        name = "staff_team",
        joinColumns = [JoinColumn(name = "staff_id")],
        inverseJoinColumns = [JoinColumn(name = "team_id")]
    )
    val teams: List<Team>?,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")  // Note: this column should not be used in general, because it can change whenever a user's teams changes. It's only used here for backward compatibility with Community API.
    val provider: Provider,
) {
    fun isUnallocated() = code.endsWith("U")
}

@Entity
@Immutable
@Table(name = "user_")
class StaffUser(

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null,

    @Column(name = "distinguished_name")
    val username: String,

    @Id
    @Column(name = "user_id")
    val id: Long
) {
    @Transient
    var email: String? = null
}

interface StaffRepository : JpaRepository<Staff, Long> {
    @EntityGraph(attributePaths = ["user", "teams"])
    fun findByUserUsername(username: String): Staff?

    @EntityGraph(attributePaths = ["user", "teams"])
    override fun findById(id: Long): Optional<Staff>

    @EntityGraph(attributePaths = ["user"])
    fun findByUserUsernameIn(usernames: List<String>): List<Staff>
}
