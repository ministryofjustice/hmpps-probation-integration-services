package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "user_")
class User(
    @Id
    @Column(name = "user_id")
    val id: Long,

    @Column(name = "staff_id")
    val staffId: Long,

    @Column(name = "distinguished_name")
    val username: String
)

interface UserRepository : JpaRepository<User, Long> {
    fun findByStaffId(staffId: Long): User?
}
