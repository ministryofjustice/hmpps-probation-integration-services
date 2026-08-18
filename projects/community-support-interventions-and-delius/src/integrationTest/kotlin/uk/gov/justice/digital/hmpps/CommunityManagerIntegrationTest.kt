package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.controller.model.CommunityManager
import uk.gov.justice.digital.hmpps.controller.model.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.controller.model.Name
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.service.LdapService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class CommunityManagerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @MockitoBean
    private lateinit var ldapService: LdapService

    @Test
    fun `get community manager returns full details`() {
        val crn = PersonGenerator.PERSON1.crn
        val staffUser = CommunityManagerGenerator.STAFF_USER
        val staff = CommunityManagerGenerator.STAFF
        val team = CommunityManagerGenerator.TEAM

        whenever(ldapService.findEmailForUsername(staffUser.userName)).thenReturn("john.smith@justice.gov.uk")
        whenever(ldapService.findTeamForUsername(staffUser.userName)).thenReturn(team.code)

        mockMvc.get("/case/$crn/community-manager") { withToken() }
            .andExpect { status { isOk() } }
            .andExpectJson(
                CommunityOffenderManager(
                    crn = crn,
                    communityManager = CommunityManager(
                        jobRole = staff.jobRole?.description,
                        emailAddress = "john.smith@justice.gov.uk",
                        pdu = CommunityManagerGenerator.PDU.description,
                        officeName = CommunityManagerGenerator.OFFICE_LOCATION.description,
                        name = Name(
                            forename = staff.forename,
                            middlenames = staff.middleName,
                            surname = staff.surname,
                        ),
                        teamPhoneNumber = team.telephone,
                    )
                )
            )
    }

    @Test
    fun `get community manager returns 404 when person not found`() {
        mockMvc.get("/case/Z999999/community-manager") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `get community manager returns 401 without token`() {
        val crn = PersonGenerator.PERSON1.crn
        mockMvc.get("/case/$crn/community-manager")
            .andExpect { status { isUnauthorized() } }
    }
}

