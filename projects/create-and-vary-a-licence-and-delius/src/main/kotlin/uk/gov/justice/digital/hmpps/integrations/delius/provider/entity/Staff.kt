package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

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
    val teams: List<Team>,


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
    @EntityGraph(attributePaths = ["user"])
    fun findByUserUsername(username: String): Staff?
}

