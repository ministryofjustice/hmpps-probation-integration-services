package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "user_")
class User(
    @Id
    @Column(name = "user_id")
    val id: Long,
    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null,
    @Column(name = "distinguished_name")
    val username: String,
)

interface UserRepository : JpaRepository<User, Long> {
    fun findByStaffId(staffId: Long): User?
}
