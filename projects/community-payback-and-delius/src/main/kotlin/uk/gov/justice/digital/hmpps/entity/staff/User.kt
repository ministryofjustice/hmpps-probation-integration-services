package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.slf4j.LoggerFactory
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

private val logger = LoggerFactory.getLogger(UserRepository::class.java)

fun UserRepository.usernamesById(ids: Collection<Long>): Map<Long, String> {
    val distinctIds = ids.toSet()
    val users = distinctIds.chunked(900)
        .flatMap { findAllById(it) }
        .associate { it.id to it.username }

    val missingIds = distinctIds - users.keys
    if (missingIds.isNotEmpty()) {
        logger.warn("Could not resolve last updated usernames for user IDs: {}", missingIds)
    }

    return users
}