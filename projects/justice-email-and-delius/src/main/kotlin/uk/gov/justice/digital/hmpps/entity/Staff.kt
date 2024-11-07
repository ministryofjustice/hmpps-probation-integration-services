package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Entity
@Immutable
class Staff(
    @Id
    @Column(name = "staff_id")
    val id: Long,

    @OneToOne(mappedBy = "staff")
    val user: StaffUser? = null,
)

@Entity
@Immutable
@Table(name = "user_")
class StaffUser(
    @Id
    @Column(name = "user_id")
    val id: Long,

    @Column(name = "distinguished_name")
    val username: String,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null,
)

interface StaffRepository : JpaRepository<Staff, Long> {
    @Query("select s from Staff s where upper(s.user.username) = upper(:username)")
    fun findByUserUsername(username: String): Staff?
}
