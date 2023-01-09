package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.ldap.core.LdapTemplate
import uk.gov.justice.digital.hmpps.data.generator.LdapUserGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.user.LdapUser
import uk.gov.justice.digital.hmpps.integrations.delius.user.LdapUserRepository

@ExtendWith(MockitoExtension::class)
class LdapServiceTest {
    @Mock lateinit var ldapUserRepository: LdapUserRepository
    @Mock lateinit var ldapTemplate: LdapTemplate
    @InjectMocks lateinit var ldapService: LdapService

    @Test
    fun `searches are split into chunks of 500`() {
        var i = 0
        val staff = (1..9999).map { StaffGenerator.generateStaffWithUser("TEST$it") }
        whenever(ldapTemplate.find(any(), eq(LdapUser::class.java))).thenAnswer {
            (1..500).map {
                val username = 500 * i + it
                LdapUserGenerator.generate("TEST$username", "TEST$username@example.com")
            }.also { i++ }
        }

        val emails = ldapService.findEmailsForStaffIn(staff)

        verify(ldapTemplate, times(20)).find(any(), eq(LdapUser::class.java))
        assertThat(emails.keys, hasSize(10000))
        assertThat(emails, hasEntry("TEST1", "TEST1@example.com"))
        assertThat(emails, hasEntry("TEST9999", "TEST9999@example.com"))
    }
}
