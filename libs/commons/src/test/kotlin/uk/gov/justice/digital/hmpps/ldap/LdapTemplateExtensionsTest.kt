package uk.gov.justice.digital.hmpps.ldap

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.DirContextOperations
import org.springframework.ldap.core.LdapTemplate
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.ldap.entity.LdapUser
import javax.naming.directory.Attributes
import javax.naming.ldap.LdapName

@ExtendWith(MockitoExtension::class)
class LdapTemplateExtensionsTest {
    @Mock
    private lateinit var ldapTemplate: LdapTemplate

    @Mock
    private lateinit var dirContextOperations: DirContextOperations

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

    @Test
    fun `add role successfully`() {
        whenever(ldapTemplate.lookupContext(any<LdapName>()))
            .thenReturn(dirContextOperations)
        whenever(dirContextOperations.getDn())
            .thenReturn(LdapName("cn=ROLE1,cn=ndRoleCatalogue,ou=Users,dc=moj,dc=com"))

        ldapTemplate.addRole(
            "john-smith",
            object : DeliusRole {
                override val description = "Role One Description"
                override val mappedRole = "MAPPED_ROLE_ONE"
                override val name = "ROLE1"
            }
        )

        val nameCapture = argumentCaptor<LdapName>()
        val attributeCapture = argumentCaptor<Attributes>()
        verify(ldapTemplate).rebind(nameCapture.capture(), eq(null), attributeCapture.capture())

        assertThat(nameCapture.firstValue.toString(), equalTo("cn=ROLE1,cn=john-smith,ou=Users"))
        assertThat(attributeCapture.firstValue["cn"].toString(), equalTo("cn: ROLE1"))
        assertThat(attributeCapture.firstValue["objectclass"].toString(), equalTo("objectclass: NDRoleAssociation, alias, top"))
    }

    @Test
    fun `unable to add unknown role`() {
        whenever(ldapTemplate.lookupContext(any<LdapName>()))
            .thenReturn(null)

        val res = assertThrows<NotFoundException> {
            ldapTemplate.addRole(
                "john-smith",
                object : DeliusRole {
                    override val description = "Unknown Description"
                    override val mappedRole = "MAPPED_ROLE_UKN"
                    override val name = "UNKNOWN"
                }
            )
        }

        assertThat(res.message, equalTo("NDeliusRole of UNKNOWN not found"))
    }

    @Test
    fun `remove role successfully`() {
        ldapTemplate.removeRole(
            "john-smith",
            object : DeliusRole {
                override val description = "Role One Description"
                override val mappedRole = "MAPPED_ROLE_ONE"
                override val name = "ROLE1"
            }
        )

        val nameCapture = argumentCaptor<LdapName>()
        verify(ldapTemplate).unbind(nameCapture.capture())

        assertThat(nameCapture.firstValue.toString(), equalTo("cn=ROLE1,cn=john-smith,ou=Users"))
    }
}
