package uk.gov.justice.digital.hmpps.user

import org.springframework.data.jpa.repository.JpaRepository

interface AuditUserRepository : JpaRepository<AuditUser, Long> {
    fun findUserByUsername(username: String): AuditUser?
}
