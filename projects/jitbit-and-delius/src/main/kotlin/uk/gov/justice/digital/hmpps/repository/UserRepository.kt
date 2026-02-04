package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.User

interface UserRepository : JpaRepository<User, Long> {
    @Query("select u from User u where upper(u.username) = upper(:username)")
    fun findUserByUsername(username: String): User?
}

fun UserRepository.userExists(username: String): Boolean = existsByUserName(username)