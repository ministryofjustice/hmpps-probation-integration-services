package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.CaseloadRepository

@ExtendWith(MockitoExtension::class)
internal class TeamServiceTest {
    @Mock
    lateinit var caseloadRepository: CaseloadRepository

    @InjectMocks
    lateinit var teamService: TeamService

    @Test
    fun `maps and returns results`() {
        whenever(caseloadRepository.findTeamsManagingCase("A123456", null)).thenReturn(listOf("ABC0001", "ABC0002"))

        val results = teamService.getTeamsManagingCase("A123456", null)

        assertThat(results.teamCodes, equalTo(listOf("ABC0001", "ABC0002")))
    }
}
