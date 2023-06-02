package uk.gov.justice.digital.hmpps.integrations.delius.user.details

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.details.entity.UserDetails

interface UserDetailsRepository : JpaRepository<UserDetails, Long> {
    fun findByUsername(username: String): UserDetails?
}
