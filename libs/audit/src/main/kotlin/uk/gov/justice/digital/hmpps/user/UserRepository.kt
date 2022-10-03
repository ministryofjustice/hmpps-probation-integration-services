package uk.gov.justice.digital.hmpps.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findUserByUsername(username: String): User?
}
