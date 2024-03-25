package uk.gov.justice.digital.hmpps.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface AuditUserRepository : JpaRepository<AuditUser, Long> {
    @Query("select u from AuditUser u where upper(u.username) = upper(:username)")
    fun findUserByUsername(username: String): AuditUser?
}
