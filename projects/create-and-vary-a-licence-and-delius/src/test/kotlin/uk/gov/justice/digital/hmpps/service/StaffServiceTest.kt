package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.ldap.core.LdapTemplate
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.entity.CaseloadRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.BoroughRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffRepository

@ExtendWith(MockitoExtension::class)
internal class StaffServiceTest {

    @Mock
    lateinit var ldapTemplate: LdapTemplate

    @Mock
    lateinit var staffRepository: StaffRepository

    @Mock
    lateinit var boroughRepository: BoroughRepository

    @Mock
    lateinit var caseloadRepository: CaseloadRepository

    @InjectMocks
    lateinit var service: StaffService

    @Test
    fun `calls caseload repository`() {
        whenever(caseloadRepository.findByStaffCodeAndRoleCode("STCDE01", "OM")).thenReturn(
            listOf(CaseloadGenerator.CASELOAD_ROLE_OM_1)
        )
        val res = service.getManagedOffenders(staffCode = "STCDE01")
        assertThat(res[0].crn, equalTo("crn0001"))
    }
}
