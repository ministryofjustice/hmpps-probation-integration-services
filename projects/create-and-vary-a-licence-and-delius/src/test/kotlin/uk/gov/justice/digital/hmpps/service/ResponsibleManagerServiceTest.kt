package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.ResponsibleOfficerRepository
import uk.gov.justice.digital.hmpps.integrations.ldap.LdapService

@ExtendWith(MockitoExtension::class)
internal class ResponsibleManagerServiceTest {
    @Mock lateinit var responsibleOfficerRepository: ResponsibleOfficerRepository

    @Mock lateinit var ldapService: LdapService

    @InjectMocks lateinit var service: ResponsibleManagerService

    @Test
    fun `does not call ldap when no staff user`() {
        val crn = "L123456"
        val staff = StaffGenerator.generateStaff("NoLdap", "No", "User")
        val cm = PersonGenerator.generateManager(staff = staff)
        val ro = PersonGenerator.generateResponsibleOfficer(communityManager = cm)

        whenever(responsibleOfficerRepository.findResponsibleOfficer(crn)).thenReturn(ro)

        val res = service.findResponsibleCommunityManager(crn)
        assertThat(res, equalTo(ro.asManager()))
        verify(ldapService, never()).findEmailByUsername(any())
    }
}
