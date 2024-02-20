package uk.gov.justice.digital.hmpps.integrations.delius.user.details

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.user.details.entity.UserDetails

interface UserDetailsRepository : JpaRepository<UserDetails, Long> {
    @Query("select u from UserDetails u where upper(u.username) = upper(:username)")
    fun findByUsername(username: String): UserDetails?
}
