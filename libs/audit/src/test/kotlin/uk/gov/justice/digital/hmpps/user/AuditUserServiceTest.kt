package uk.gov.justice.digital.hmpps.user

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class AuditUserServiceTest {
    @Mock
    private lateinit var auditUserRepository: AuditUserRepository

    @InjectMocks
    private lateinit var auditUserService: AuditUserService

    @Test
    fun `user service calls repository with same values`() {
        val username = "ServiceUsername"
        auditUserService.findUser(username)
        verify(auditUserRepository).findUserByUsername(username)
    }
}
