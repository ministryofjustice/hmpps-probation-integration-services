package uk.gov.justice.digital.hmpps.ldap

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.ldap.NameNotFoundException
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.DirContextOperations
import org.springframework.ldap.core.LdapTemplate
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.ldap.entity.LdapUser
import javax.naming.Name
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
        val expected = LdapUser(LdapName("cn=test"), "test", "test@example.com")
        whenever(ldapTemplate.find(any(), eq(LdapUser::class.java))).thenReturn(listOf(expected))

        val user = ldapTemplate.findByUsername<LdapUser>("test")

        assertThat(user, equalTo(expected))
    }

    @Test
    fun `find by username returns null if not found`() {
        whenever(ldapTemplate.find(any(), eq(LdapUser::class.java))).thenReturn(emptyList())
        val user = ldapTemplate.findByUsername<LdapUser>("test")
        assertThat(user, nullValue())
    }

    @Test
    fun `find by username returns null if multiple found`() {
        whenever(ldapTemplate.find(any(), eq(LdapUser::class.java)))
            .thenReturn(List(2) { LdapUser(LdapName("cn=test"), "test", "test@example.com") })
        val user = ldapTemplate.findByUsername<LdapUser>("test")
        assertThat(user, nullValue())
    }

    @Test
    fun `find email by username`() {
        whenever(ldapTemplate.search(any(), any<AttributesMapper<String?>>()))
            .thenReturn(listOf("test@example.com"))

        val email = ldapTemplate.findEmailByUsername("test")

        assertThat(email, equalTo("test@example.com"))
    }

    @Test
    fun `find preference by username`() {
        whenever(ldapTemplate.search(any(), any<AttributesMapper<String?>>()))
            .thenReturn(listOf("test preference"))

        val preference = ldapTemplate.findPreferenceByUsername("test", "attribute")

        assertThat(preference, equalTo("test preference"))
    }

    @Test
    fun `find preference by username throws not found`() {
        whenever(ldapTemplate.search(any(), any<AttributesMapper<String?>>()))
            .thenThrow(NameNotFoundException("not found"))

        assertThrows<NotFoundException> { ldapTemplate.findPreferenceByUsername("test", "attribute") }
            .also { assertThat(it.message, equalTo("User preferences with username of test not found")) }
    }

    @Test
    fun `get roles`() {
        whenever(ldapTemplate.search(any(), any<AttributesMapper<String?>>()))
            .thenReturn(listOf("ROLE1", "ROLE2", null))

        val roles = ldapTemplate.getRoles("test")

        assertThat(roles, equalTo(listOf("ROLE1", "ROLE2")))
    }

    @Test
    fun `add role successfully`() {
        whenever(ldapTemplate.lookup(any<LdapName>())).thenThrow(NameNotFoundException("no existing role"))
        whenever(ldapTemplate.lookupContext(any<LdapName>()))
            .thenReturn(dirContextOperations)
        whenever(dirContextOperations.nameInNamespace)
            .thenReturn("cn=ROLE1,cn=ndRoleCatalogue,ou=Users,dc=moj,dc=com")

        ldapTemplate.addRole(
            "john-smith",
            object : DeliusRole {
                override val description = "Role One Description"
                override val mappedRole = "MAPPED_ROLE_ONE"
                override val name = "ROLE1"
            }
        )

        verify(ldapTemplate).rebind(check<LdapName> {
            assertThat(it.toString(), equalTo("cn=ROLE1,cn=john-smith"))
        }, eq(null), check<Attributes> {
            assertThat(it["cn"].toString(), equalTo("cn: ROLE1"))
            assertThat(
                it["aliasedObjectName"].toString(),
                equalTo("aliasedObjectName: cn=ROLE1,cn=ndRoleCatalogue,ou=Users,dc=moj,dc=com")
            )
            assertThat(it["objectclass"].toString(), equalTo("objectclass: NDRoleAssociation, alias, top"))
        })
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

        assertThat(res.message, equalTo("Role with name of UNKNOWN not found"))
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

        verify(ldapTemplate).unbind(check<LdapName> {
            assertThat(it.toString(), equalTo("cn=ROLE1,cn=john-smith"))
        })
    }

    @Test
    fun `unknown username throws NotFoundException when getting roles`() {

        whenever(ldapTemplate.search(any(), any<AttributesMapper<String?>>()))
            .thenThrow(NameNotFoundException("No Such Object"))

        assertThrows<NotFoundException> { ldapTemplate.getRoles("test") }
    }

    @Test
    fun `unknown username throws NotFoundException finding by username`() {

        whenever(ldapTemplate.search(any(), any<AttributesMapper<String?>>()))
            .thenThrow(NameNotFoundException("No Such Object"))

        assertThrows<NotFoundException> { ldapTemplate.findEmailByUsername("test") }
    }

    @Test
    fun `unknown username throws NotFoundException when adding roles`() {
        whenever(ldapTemplate.lookupContext(any<LdapName>())).thenReturn(dirContextOperations)
        whenever(dirContextOperations.nameInNamespace)
            .thenReturn("cn=ROLE1,cn=ndRoleCatalogue,ou=Users,dc=moj,dc=com")

        whenever(ldapTemplate.lookup(any<LdapName>())).thenThrow(NameNotFoundException("no existing role"))
        whenever(ldapTemplate.rebind(any<Name>(), anyOrNull(), any<Attributes>()))
            .thenThrow(NameNotFoundException("no user"))

        assertThrows<NotFoundException> {
            ldapTemplate.addRole(
                "test",
                object : DeliusRole {
                    override val description = "Role One Description"
                    override val mappedRole = "MAPPED_ROLE_ONE"
                    override val name = "ROLE1"
                }
            )
        }
    }

    @Test
    fun `unknown username throws NotFoundException when removing roles`() {
        whenever(ldapTemplate.lookup(any<LdapName>())).thenReturn("existing role")
        whenever(ldapTemplate.unbind(any<Name>())).thenThrow(NameNotFoundException("no user"))

        assertThrows<NotFoundException> {
            ldapTemplate.removeRole(
                "test",
                object : DeliusRole {
                    override val description = "Role One Description"
                    override val mappedRole = "MAPPED_ROLE_ONE"
                    override val name = "ROLE1"
                }
            )
        }
    }

    @Test
    fun `find emails by usernames`() {
        whenever(ldapTemplate.search(any(), any<AttributesMapper<Pair<String?, String?>>>())).thenReturn(
            listOf(
                "user1" to "user1@example.com",
                "user2" to "user2@example.com",
                "user3" to null,
                null to null,
            )
        )

        val emails = ldapTemplate.findEmailByUsernames(listOf("user1", "user2", "user3"))

        assertThat(
            emails, equalTo(
                mapOf(
                    "user1" to "user1@example.com",
                    "user2" to "user2@example.com",
                    "user3" to null
                )
            )
        )
    }

    @Test
    fun `searches are split into chunks of 500`() {
        var i = 0
        val usernames = (1..9999).map { "TEST$it" }
        whenever(ldapTemplate.search(any(), any<AttributesMapper<Pair<String?, String?>>>())).thenAnswer {
            (1..500).map { "TEST${500 * i + it}" }.map { username -> username to "$username@example.com" }.also { i++ }
        }

        val emails = ldapTemplate.findEmailByUsernames(usernames)

        verify(ldapTemplate, times(20)).search(any(), any<AttributesMapper<Pair<String?, String?>>>())
        assertThat(emails.keys, hasSize(10000))
        assertThat(emails, hasEntry("TEST1", "TEST1@example.com"))
        assertThat(emails, hasEntry("TEST9999", "TEST9999@example.com"))
    }
}
