package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.UserAccessRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.details.UserDetailsRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.details.entity.UserDetails

@ExtendWith(MockitoExtension::class)
internal class UserServiceTest {
    @Mock
    lateinit var userDetailsRepository: UserDetailsRepository

    @Mock
    lateinit var userAccessRepository: UserAccessRepository

    @Mock
    lateinit var ldapTemplate: LdapTemplate

    @InjectMocks
    lateinit var userService: UserService

    @Test
    fun `user doesn't exist`() {
        whenever(userAccessRepository.existsByCrn("123")).thenReturn(false)

        assertThrows<NotFoundException> {
            userService.checkUserAccess("test", "123")
        }
    }

    @Test
    fun `get user details`() {
        val username = "test"
        whenever(userDetailsRepository.findByUsername(username))
            .thenReturn(UserDetails(1, username, "forename", "forename2", "surname", null))
        whenever(ldapTemplate.search(any(), any<AttributesMapper<String?>>()))
            .thenReturn(listOf("test@example.com"))

        val user = userService.getUserDetails(username)!!

        assertThat(user.username, equalTo(username))
        assertThat(user.email, equalTo("test@example.com"))
        assertThat(user.name.forename, equalTo("forename"))
        assertThat(user.staffCode, nullValue())
    }
}
