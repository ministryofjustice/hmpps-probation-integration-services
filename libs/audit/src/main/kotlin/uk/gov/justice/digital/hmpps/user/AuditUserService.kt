package uk.gov.justice.digital.hmpps.user

import org.springframework.stereotype.Service

@Service
class AuditUserService(private val auditUserRepository: AuditUserRepository) {
    fun findUser(username: String): AuditUser? {
        return auditUserRepository.findUserByUsername(username)
    }
}
