package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.entity.CaseloadRepository

@ExtendWith(MockitoExtension::class)
internal class TeamServiceTest {

    @Mock
    lateinit var caseloadRepository: CaseloadRepository

    @InjectMocks
    lateinit var service: TeamService

    @Test
    fun `calls caseload repository`() {
        whenever(caseloadRepository.findByTeamCodeAndRoleCodeOrderByAllocationDateDesc("N01BDT", "OM")).thenReturn(
            listOf(CaseloadGenerator.CASELOAD_ROLE_OM_1)
        )
        val res = service.getManagedOffendersByTeam("N01BDT")
        assertThat(res[0].crn, equalTo("crn0001"))
    }
}
