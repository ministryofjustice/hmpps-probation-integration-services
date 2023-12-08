package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.ldap.core.LdapTemplate
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProviderRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.UserAccessRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.details.UserDetailsRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.details.entity.UserDetails
import uk.gov.justice.digital.hmpps.integrations.delius.user.ldap.entity.LdapUser
import javax.naming.ldap.LdapName

@ExtendWith(MockitoExtension::class)
internal class UserServiceTest {
    @Mock
    lateinit var userDetailsRepository: UserDetailsRepository

    @Mock
    lateinit var providerRepository: ProviderRepository

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
        val ldapUser =
            LdapUser(
                dn = LdapName("uid=test,ou=users,dc=example,dc=com"),
                username = "test",
                email = "test@example.com",
                homeArea = "N01",
            )
        whenever(userDetailsRepository.findByUsername(username))
            .thenReturn(UserDetails(1, username, "forename", "forename2", "surname", null))
        whenever(providerRepository.findByCode("N01"))
            .thenReturn(Provider(1, "N01", "London"))
        whenever(ldapTemplate.find(any(), eq(LdapUser::class.java))).thenReturn(listOf(ldapUser))

        val user = userService.getUserDetails(username)!!

        assertThat(user.username, equalTo(username))
        assertThat(user.email, equalTo("test@example.com"))
        assertThat(user.name.forename, equalTo("forename"))
        assertThat(user.staffCode, nullValue())
        assertThat(user.homeArea!!.code, equalTo("N01"))
        assertThat(user.homeArea!!.name, equalTo("London"))
    }
}
