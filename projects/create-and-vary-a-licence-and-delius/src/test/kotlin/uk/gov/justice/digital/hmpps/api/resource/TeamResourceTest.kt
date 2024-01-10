package uk.gov.justice.digital.hmpps.api.resource

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator.generateManagedOffender
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.service.TeamService

@ExtendWith(MockitoExtension::class)
internal class TeamResourceTest {

    @Mock
    lateinit var teamService: TeamService

    @InjectMocks
    lateinit var resource: TeamResource

    @Test
    fun `calls managed offenders endpoint`() {
        whenever(teamService.getManagedOffendersByTeam("N01BDT")).thenReturn(
            listOf(
                generateManagedOffender(
                    CaseloadGenerator.CASELOAD_ROLE_OM_1, CaseloadGenerator.STAFF1, ProviderGenerator.DEFAULT_TEAM
                )
            )

        )
        val res = resource.getManagedOffendersByTeam("N01BDT")
        assertThat(res[0].crn, equalTo("crn0001"))
    }
}
