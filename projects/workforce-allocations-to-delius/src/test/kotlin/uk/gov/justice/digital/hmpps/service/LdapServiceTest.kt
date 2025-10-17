package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffUserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffWithUser

@ExtendWith(MockitoExtension::class)
class LdapServiceTest {
    @Mock
    lateinit var ldapTemplate: LdapTemplate

    @InjectMocks
    lateinit var ldapService: LdapService

    @Test
    fun `email found for single staff`() {
        val staff = StaffGenerator.generateStaffWithUser("email", user = StaffUserGenerator.generate("HasEmail"))
        whenever(ldapTemplate.search(any(), any<AttributesMapper<String?>>())).thenReturn(listOf("email@user.com"))

        val email = ldapService.findEmailForStaff(staff)

        assertThat(email, equalTo("email@user.com"))
    }

    @ParameterizedTest
    @MethodSource("noEmail")
    fun `find email returns null`(staff: StaffWithUser?) {
        val email = ldapService.findEmailForStaff(staff)
        assertNull(email)
    }

    companion object {
        @JvmStatic
        fun noEmail(): List<StaffWithUser?> = listOf(
            null,
            StaffGenerator.generateStaffWithUser("NoUser", user = null),
            StaffGenerator.generateStaffWithUser("NoLdapUser", user = StaffUserGenerator.generate("NoLdapUser"))
        )
    }
}
