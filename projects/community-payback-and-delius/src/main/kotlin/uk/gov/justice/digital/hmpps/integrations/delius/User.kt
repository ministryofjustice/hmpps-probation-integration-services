package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Entity
@Immutable
@Table(name = "user_")
class User (
    @Id
    @Column(name = "user_id")
    val id: Long,

    @Column(name = "distinguished_name")
    val username: String
)

interface UserRepository : JpaRepository<User, Long> {
    @Query("select u from User u where upper(u.username) = upper(:username)")
    fun findUserByUsername(username: String): User?
}
