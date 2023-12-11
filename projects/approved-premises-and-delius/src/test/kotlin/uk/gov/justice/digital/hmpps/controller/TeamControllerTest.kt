package uk.gov.justice.digital.hmpps.controller

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.service.TeamService

@ExtendWith(MockitoExtension::class)
internal class TeamControllerTest {
    @Mock
    lateinit var teamService: TeamService

    @InjectMocks
    lateinit var teamController: TeamController

    @Test
    fun `calls the service`() {
        teamController.getTeamsManagingCase("A", "B")
        verify(teamService).getTeamsManagingCase("A", "B")
    }
}
