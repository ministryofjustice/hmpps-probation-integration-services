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
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.security.ServiceContext
import uk.gov.justice.digital.hmpps.security.ServicePrincipal
import uk.gov.justice.digital.hmpps.user.User
import uk.gov.justice.digital.hmpps.user.UserService

@ExtendWith(MockitoExtension::class)
internal class AuditorAwareTest {

    @Mock
    private lateinit var securityContext: SecurityContext

    @Mock
    private lateinit var authentication: Authentication

    @Mock
    private lateinit var userService: UserService

    @Mock

    private lateinit var applicationStartedEvent: ApplicationStartedEvent

    private lateinit var servicePrincipal: ServicePrincipal

    @InjectMocks
    private lateinit var auditorAware: AuditorAware

    private val user = User(1, "ServiceUserName")

    @BeforeEach
    fun setUp() {
        servicePrincipal = ServicePrincipal(user.username, user.id)
    }

    @Test
    fun `get current auditor contains authentication principal username`() {
        whenever(securityContext.authentication).thenReturn(authentication)
        whenever(authentication.principal).thenReturn(servicePrincipal)

        SecurityContextHolder.setContext(securityContext)
        val opt = auditorAware.currentAuditor

        assertThat(opt.isPresent)
    }

    @Test
    fun `get current auditor contains service context username`() {
        whenever(securityContext.authentication).thenReturn(authentication)
        whenever(userService.findUser(user.username)).thenReturn(user)

        ServiceContext(user.username, userService).onApplicationEvent(applicationStartedEvent)

        SecurityContextHolder.setContext(securityContext)
        val opt = auditorAware.currentAuditor

        assertThat(opt.isPresent)
    }
}
