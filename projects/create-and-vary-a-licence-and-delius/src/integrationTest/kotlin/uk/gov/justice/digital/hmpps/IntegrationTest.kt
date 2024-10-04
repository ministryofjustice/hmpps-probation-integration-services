package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.asAddress
import uk.gov.justice.digital.hmpps.service.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `returns responsible officer details`() {
        val crn = PersonGenerator.DEFAULT_PERSON.crn

        val manager = mockMvc
            .perform(get("/probation-case/$crn/responsible-community-manager").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<Manager>()


        assertThat(
            manager,
            equalTo(
                PersonGenerator.DEFAULT_CM.asManager().copy(
                    username = "john-smith", email = "john.smith@moj.gov.uk"
                )
            )
        )
    }

    @Test
    fun `returns responsible officer details for a list of CRNs`() {
        val crn = PersonGenerator.DEFAULT_PERSON.crn
        mockMvc
            .perform(post("/probation-case/responsible-community-manager").withToken().withJson(listOf(crn)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("size()", equalTo(1)))
            .andExpect(jsonPath("$[0].code", equalTo("N01BDT1")))
            .andExpect(jsonPath("$[0].email", equalTo("john.smith@moj.gov.uk")))
    }

    @Test
    fun `returns only active and non null team office locations`() {
        val crn = PersonGenerator.PERSON_ENDED_TEAM_LOCATION.crn
        val expectedAddresses: List<OfficeAddress> = listOf(
            ProviderGenerator.LOCATION_BRK_1.asAddress(),
            ProviderGenerator.LOCATION_BRK_2.asAddress()
        )
        val manager = mockMvc
            .perform(get("/probation-case/$crn/responsible-community-manager").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<Manager>()
        assertThat(
            manager,
            equalTo(
                PersonGenerator.CM_ENDED_TEAM_LOCATION.asManager().copy(
                    username = "john-smith", email = "john.smith@moj.gov.uk",
                    team = ProviderGenerator.TEAM_ENDED_OR_NULL_LOCATIONS.asTeam().copy(addresses = expectedAddresses)
                )
            )
        )
    }

    @Test
    fun `returns 404 if no crn or community officer`() {
        mockMvc.perform(
            get("/probation-case/Z123456/responsible-community-manager")
                .withToken()
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `returns staff details`() {
        val username = StaffGenerator.DEFAULT_STAFF_USER.username
        mockMvc
            .perform(get("/staff/$username").withToken())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username", equalTo("john-smith")))
            .andExpect(jsonPath("$.email", equalTo("john.smith@moj.gov.uk")))
            .andExpect(jsonPath("$.telephoneNumber", equalTo("10101010101")))
    }

    @Test
    fun `username is case-insensitive`() {
        val username = StaffGenerator.DEFAULT_STAFF_USER.username.uppercase()
        mockMvc
            .perform(get("/staff/$username").withToken())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username", equalTo("john-smith")))
            .andExpect(jsonPath("$.email", equalTo("john.smith@moj.gov.uk")))
            .andExpect(jsonPath("$.telephoneNumber", equalTo("10101010101")))
    }

    @Test
    fun `returns pdu heads`() {
        val boroughCode = ProviderGenerator.DEFAULT_BOROUGH.code

        val pduHeads = mockMvc
            .perform(get("/staff/$boroughCode/pdu-head").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<List<PDUHead>>()

        assertThat(
            pduHeads,
            equalTo(
                listOf(
                    StaffGenerator.PDUHEAD.asPDUHead().copy(email = "bob.smith@moj.gov.uk")
                )
            )
        )
    }

    @Test
    fun `returns staff names for usernames`() {
        val usernames =
            listOf(StaffGenerator.DEFAULT_PDUSTAFF_USER.username, StaffGenerator.DEFAULT_STAFF_USER.username)

        val staffNames = mockMvc
            .perform(post("/staff").withToken().withJson(usernames))
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<List<StaffName>>()

        assertThat(
            staffNames,
            equalTo(
                listOf(
                    StaffGenerator.PDUHEAD.asStaffName(),
                    StaffGenerator.DEFAULT.asStaffName()
                )
            )
        )
    }

    @Test
    fun `usernames are case-insensitive`() {
        val usernames = listOf(
            StaffGenerator.DEFAULT_PDUSTAFF_USER.username.uppercase(),
            StaffGenerator.DEFAULT_STAFF_USER.username.uppercase()
        )

        val staffNames = mockMvc
            .perform(post("/staff").withToken().withJson(usernames))
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<List<StaffName>>()

        assertThat(
            staffNames,
            equalTo(
                listOf(
                    StaffGenerator.PDUHEAD.asStaffName(),
                    StaffGenerator.DEFAULT.asStaffName()
                )
            )
        )
    }

    @Test
    fun `returns staff by id `() {
        mockMvc
            .perform(get("/staff/byid/${StaffGenerator.DEFAULT.id}").withToken())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username", equalTo("john-smith")))
            .andExpect(jsonPath("$.email", equalTo("john.smith@moj.gov.uk")))
            .andExpect(jsonPath("$.telephoneNumber", equalTo("10101010101")))
    }

    @Test
    fun `returns staff by id not found `() {

        mockMvc.perform(get("/staff/byid/9999999").withToken()).andExpect(status().isNotFound)
    }

    @Test
    fun `returns staff by code`() {
        mockMvc
            .perform(get("/staff/bycode/${StaffGenerator.DEFAULT.code}").withToken())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username", equalTo("john-smith")))
            .andExpect(jsonPath("$.email", equalTo("john.smith@moj.gov.uk")))
            .andExpect(jsonPath("$.telephoneNumber", equalTo("10101010101")))
    }
}
