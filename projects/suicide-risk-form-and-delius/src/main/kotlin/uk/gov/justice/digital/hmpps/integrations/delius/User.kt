package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Immutable
@Table(name = "user_")
class User(
    @Id
    @Column(name = "user_id")
    val id: Long,

    @Column(name = "distinguished_name")
    val username: String,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff?,
)

interface UserRepository: JpaRepository<User, Long> {
    fun findByStaffId(staffId: Long): User?
}