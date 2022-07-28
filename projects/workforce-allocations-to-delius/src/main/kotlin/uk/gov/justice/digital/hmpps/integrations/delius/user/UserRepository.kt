package uk.gov.justice.digital.hmpps.integrations.delius.user

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.audit.User

interface UserRepository : JpaRepository<User, Long> {
    fun findUserByUsername(username: String): User?
}
