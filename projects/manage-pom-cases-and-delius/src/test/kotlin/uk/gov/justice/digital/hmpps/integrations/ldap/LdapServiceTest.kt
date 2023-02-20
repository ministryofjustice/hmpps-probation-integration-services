package uk.gov.justice.digital.hmpps.integrations.ldap

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.ldap.core.LdapTemplate
import javax.naming.ldap.LdapName

@ExtendWith(MockitoExtension::class)
class LdapServiceTest {
    @Mock
    private lateinit var ldapTemplate: LdapTemplate

    @InjectMocks
    private lateinit var ldapService: LdapService

    @Test
    fun `find email by username`() {
        val user = LdapUser(LdapName("cn=test,ou=Users"), "test", "test@example.com")
        whenever(ldapTemplate.find(any(), eq(LdapUser::class.java))).thenReturn(listOf(user))

        val email = ldapService.findEmailByUsername("test")

        assertThat(email, equalTo("test@example.com"))
    }
}
