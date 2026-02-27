package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.user.User

interface UserRepository : JpaRepository<User, Long> {
    @Query("select u from User u left join fetch u.staff where upper(u.username) = upper(:username)")
    fun findByUsername(username: String): User?
}