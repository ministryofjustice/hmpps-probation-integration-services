package uk.gov.justice.digital.hmpps.integrations.delius.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.User

interface UserRepository : JpaRepository<User, Long> {
    fun findUserByUsername(username: String): User?
}