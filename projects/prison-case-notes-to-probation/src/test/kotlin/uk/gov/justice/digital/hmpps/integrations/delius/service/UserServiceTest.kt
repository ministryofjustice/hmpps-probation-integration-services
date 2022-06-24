package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.repository.UserRepository

@ExtendWith(MockitoExtension::class)
class UserServiceTest {
    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var authentication: Authentication

    @Mock
    lateinit var securityContext: SecurityContext

    @InjectMocks
    lateinit var userService: UserService

    @Test
    fun `unable to get user from principal`() {
        whenever(securityContext.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(securityContext)
        val ex = assertThrows<IllegalAccessException> {
            userService.findServiceUser()
        }
        assertThat(ex.message, equalTo("Unable to get username from principal"))
    }

    @Test
    fun `unable to retrieve user from database`() {
        val name = UserGenerator.APPLICATION_USER.username
        val userDetails = User(name, "", listOf())
        whenever(securityContext.authentication).thenReturn(authentication)
        whenever(securityContext.authentication.principal).thenReturn(userDetails)
        whenever(userRepository.findUserByUsername(name)).thenReturn(null)

        SecurityContextHolder.setContext(securityContext)
        val ex = assertThrows<IllegalAccessException> {
            userService.findServiceUser()
        }
        assertThat(ex.message, equalTo("Service User does not exist in the database"))
    }
}
