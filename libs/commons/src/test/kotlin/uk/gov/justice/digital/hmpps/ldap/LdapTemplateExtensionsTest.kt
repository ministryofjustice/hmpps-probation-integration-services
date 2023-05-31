package uk.gov.justice.digital.hmpps.ldap

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import uk.gov.justice.digital.hmpps.ldap.entity.LdapUser
import javax.naming.ldap.LdapName

@ExtendWith(MockitoExtension::class)
class LdapTemplateExtensionsTest {
    @Mock
    private lateinit var ldapTemplate: LdapTemplate

    @Test
    fun `find by username`() {
        val expected = LdapUser(LdapName("cn=test,ou=Users"), "test", "test@example.com")
        whenever(ldapTemplate.find(any(), eq(LdapUser::class.java))).thenReturn(listOf(expected))

        val user = ldapTemplate.findByUsername<LdapUser>("test")

        assertThat(user, equalTo(expected))
    }

    @Test
    fun `find email by username`() {
        whenever(ldapTemplate.search(any(), any<AttributesMapper<String?>>()))
            .thenReturn(listOf("test@example.com"))

        val email = ldapTemplate.findEmailByUsername("test")

        assertThat(email, equalTo("test@example.com"))
    }
}
