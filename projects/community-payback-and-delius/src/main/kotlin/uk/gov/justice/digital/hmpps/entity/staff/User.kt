package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy

@Entity
@Immutable
@Table(name = "user_")
class User(
    @Id
    @Column(name = "user_id")
    val id: Long,

    @Column(name = "distinguished_name")
    val username: String,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null
)

interface UserRepository : JpaRepository<User, Long> {
    @Query(
        """
        select u from User u 
        left join fetch u.staff s
        left join fetch s.grade
        left join fetch s.teams
        where upper(u.username) = upper(:username)
        """
    )
    fun findByUsername(username: String): User?
}

fun UserRepository.getByUsername(username: String) = findByUsername(username).orNotFoundBy("username", username)