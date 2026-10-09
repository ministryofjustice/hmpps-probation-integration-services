package uk.gov.justice.digital.hmpps.api.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.ActiveCasesResponse
import uk.gov.justice.digital.hmpps.api.model.TeamActiveCasesResponse
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.service.TeamService

@ExtendWith(MockitoExtension::class)
internal class TeamResourceTest {

    @Mock
    lateinit var teamService: TeamService

    @InjectMocks
    lateinit var resource: TeamResource

    @Test
    fun `calls active cases endpoint`() {
        val response = TeamActiveCasesResponse(
            code = "N02ABS",
            description = "Allocation Team",
            staff = listOf(
                ActiveCasesResponse(
                    code = "N02ABS1",
                    name = Name("Joe", null, "Bloggs"),
                    grade = "PSO",
                    email = "joe.bloggs@example.com",
                    cases = emptyList()
                )
            )
        )
        whenever(teamService.getActiveCases("N02ABS")).thenReturn(response)

        val res = resource.activeCases("N02ABS")

        assertThat(res).isEqualTo(response)
    }
}

