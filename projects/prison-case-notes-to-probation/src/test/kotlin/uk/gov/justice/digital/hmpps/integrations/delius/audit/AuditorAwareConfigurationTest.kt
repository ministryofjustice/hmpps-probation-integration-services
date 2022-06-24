package uk.gov.justice.digital.hmpps.integrations.delius.audit

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.service.UserService

@ExtendWith(MockitoExtension::class)
internal class AuditorAwareConfigurationTest {

    @Mock
    private lateinit var securityContext: SecurityContext

    @Mock
    private lateinit var authentication: Authentication

    @Mock
    private lateinit var userDetails: UserDetails

    @Mock
    private lateinit var userService: UserService

    @InjectMocks
    private lateinit var auditorAwareConfiguration: AuditorAwareConfiguration

    @Test
    fun `get current auditor contains db username`() {
        val user = UserGenerator.APPLICATION_USER
        whenever(securityContext.authentication).thenReturn(authentication)
        whenever(authentication.principal).thenReturn(userDetails)
        whenever(userDetails.username).thenReturn(user.username)
        whenever(userService.findServiceUser(user.username)).thenReturn(user)

        SecurityContextHolder.setContext(securityContext)
        val opt = auditorAwareConfiguration.currentAuditor

        verify(userService).findServiceUser(user.username)
        assertThat(opt.isPresent)
    }

    @Test
    fun `no security context set returns empty optional`() {
        whenever(securityContext.authentication).thenReturn(authentication)

        SecurityContextHolder.setContext(securityContext)
        val opt = auditorAwareConfiguration.currentAuditor

        verify(userService, times(0)).findServiceUser(anyString())
        assertThat(opt.isEmpty)
    }
}