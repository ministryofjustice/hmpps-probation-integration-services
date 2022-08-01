package uk.gov.justice.digital.hmpps.integrations.delius.audit

import UserGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.audit.service.AuditorAware
import uk.gov.justice.digital.hmpps.config.security.ServicePrincipal

@ExtendWith(MockitoExtension::class)
internal class AuditorAwareConfigurationTest {

    @Mock
    private lateinit var securityContext: SecurityContext

    @Mock
    private lateinit var authentication: Authentication

    private lateinit var servicePrincipal: ServicePrincipal

    @InjectMocks
    private lateinit var auditorAware: AuditorAware

    @BeforeEach
    fun setUp() {
        servicePrincipal = ServicePrincipal("prison-case-notes-to-probation", UserGenerator.APPLICATION_USER.id)
    }

    @Test
    fun `get current auditor contains db username`() {
        whenever(securityContext.authentication).thenReturn(authentication)
        whenever(authentication.principal).thenReturn(servicePrincipal)

        SecurityContextHolder.setContext(securityContext)
        val opt = auditorAware.currentAuditor

        assertThat(opt.isPresent)
    }

    @Test
    fun `no security context set returns empty optional`() {
        whenever(securityContext.authentication).thenReturn(authentication)

        SecurityContextHolder.setContext(securityContext)
        val opt = auditorAware.currentAuditor

        assertThat(opt.isEmpty)
    }

    @Test
    fun `no authentication set returns empty optional`() {
        whenever(securityContext.authentication).thenReturn(null)

        SecurityContextHolder.setContext(securityContext)
        val opt = auditorAware.currentAuditor

        assertThat(opt.isEmpty)
    }
}
