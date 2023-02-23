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
import uk.gov.justice.digital.hmpps.user.User
import uk.gov.justice.digital.hmpps.user.UserService

@ExtendWith(MockitoExtension::class)
internal class AuditorAwareTest {

    @Mock
    private lateinit var userService: UserService

    @Mock
    private lateinit var applicationStartedEvent: ApplicationStartedEvent

    @InjectMocks
    private lateinit var auditorAware: AuditorAware

    private val user = User(1, "ServiceUserName")

    @BeforeEach
    fun setUpUser() {
        whenever(userService.findUser(user.username)).thenReturn(user)
        ServiceContext(user.username, userService).onApplicationEvent(applicationStartedEvent)
    }

    @Test
    fun `get current auditor contains authentication principal username`() {
        val opt = auditorAware.currentAuditor
        assertThat(opt.isPresent)
    }
}
