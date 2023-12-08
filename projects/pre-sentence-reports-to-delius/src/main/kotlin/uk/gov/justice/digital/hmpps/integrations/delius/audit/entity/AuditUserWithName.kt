package uk.gov.justice.digital.hmpps.integrations.delius.audit.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@Table(name = "user_")
class User(
    @Id
    @Column(name = "user_id")
    val id: Long,
    @Column(name = "distinguished_name")
    val username: String,
    val surname: String,
    val forename: String,
)

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User
}
