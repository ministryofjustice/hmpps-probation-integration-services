package uk.gov.justice.digital.hmpps.audit.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.boot.context.event.ApplicationStartedEvent
import uk.gov.justice.digital.hmpps.security.ServiceContext
import uk.gov.justice.digital.hmpps.user.AuditUser
import uk.gov.justice.digital.hmpps.user.AuditUserService

@ExtendWith(MockitoExtension::class)
internal class AuditorAwareTest {
    @Mock
    private lateinit var auditUserService: AuditUserService

    @Mock
    private lateinit var applicationStartedEvent: ApplicationStartedEvent

    @InjectMocks
    private lateinit var auditorAware: AuditorAware

    private val user = AuditUser(1, "ServiceUserName")

    @BeforeEach
    fun setUpUser() {
        whenever(auditUserService.findUser(user.username)).thenReturn(user)
        ServiceContext(user.username, auditUserService).onApplicationEvent(applicationStartedEvent)
    }

    @Test
    fun `get current auditor contains authentication principal username`() {
        val opt = auditorAware.currentAuditor
        assertThat(opt.isPresent)
    }
}
