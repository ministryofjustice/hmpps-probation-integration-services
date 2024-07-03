package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.data.generator.NsiManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.RequirementsGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.BREACH_NSIS
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.service.toProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.service.toTeam
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class NsisByCrnAndConvictionIdIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `unauthorized status returned`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        mockMvc
            .perform(get("/probation-case/$crn/convictions/1/nsis"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `request params not provided`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        mockMvc
            .perform(get("/probation-case/$crn/convictions/1/nsis").withToken())
            .andExpect(status().isBadRequest)
            .andExpect(status().reason("Required parameter 'nsiCodes' is not present."))
    }

    @Test
    fun `probation record not found`() {
        mockMvc
            .perform(
                get("/probation-case/A123456/convictions/123/nsis")
                    .param("nsiCodes", "{}")
                    .withToken()
            )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Person with crn of A123456 not found"))
    }

    @Test
    fun `sentence not found`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn

        mockMvc
            .perform(
                get("/probation-case/$crn/convictions/3/nsis")
                    .param("nsiCodes", "{}")
                    .withToken()
            )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Conviction with ID 3 for Offender with crn C123456 not found"))
    }

    @Test
    fun `return list of nsis`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        val event = SentenceGenerator.CURRENTLY_MANAGED

        val managers =
            listOf(
                NsiManager(
                    NsiManagerGenerator.ACTIVE.probationArea.toProbationArea(false),
                    NsiManagerGenerator.ACTIVE.team.toTeam(),
                    NsiManagerGenerator.ACTIVE.staff.toStaffDetails(),
                    NsiManagerGenerator.ACTIVE.startDate,
                    NsiManagerGenerator.ACTIVE.endDate
                ),
                NsiManager(
                    NsiManagerGenerator.ACTIVE.probationArea.toProbationArea(false),
                    NsiManagerGenerator.ACTIVE.team.toTeam(),
                    NsiManagerGenerator.ACTIVE.staff.toStaffDetails(),
                    NsiManagerGenerator.INACTIVE.startDate,
                    NsiManagerGenerator.INACTIVE.endDate
                )
            )

        val expectedResponse = NsiDetails(
            listOf(
                Nsi(
                    BREACH_NSIS.id,
                    KeyValue(BREACH_NSIS.type.code, BREACH_NSIS.type.description),
                    null,
                    KeyValue(BREACH_NSIS.outcome!!.code, BREACH_NSIS.outcome!!.description),
                    RequirementsGenerator.ACTIVE_REQ.toRequirementModel(),
                    KeyValue(BREACH_NSIS.nsiStatus.code, BREACH_NSIS.nsiStatus.description),
                    BREACH_NSIS.statusDate,
                    BREACH_NSIS.actualStartDate,
                    BREACH_NSIS.expectedStartDate,
                    BREACH_NSIS.actualStartDate,
                    BREACH_NSIS.expectedEndDate,
                    BREACH_NSIS.referralDate,
                    BREACH_NSIS.length,
                    "Months",
                    managers,
                    BREACH_NSIS.notes,
                    BREACH_NSIS.intendedProvider?.toProbationArea(true),
                    BREACH_NSIS.active,
                    BREACH_NSIS.softDeleted,
                    BREACH_NSIS.externalReference
                )
            )
        )

        val response = mockMvc
            .perform(
                get("/probation-case/$crn/convictions/${event.id}/nsis")
                    .param("nsiCodes", "NSI type")
                    .withToken()
            )
            .andExpect(status().isOk)
            .andDo(MockMvcResultHandlers.print())
            .andReturn().response.contentAsJson<NsiDetails>()

        assertEquals(expectedResponse, response)
    }

    fun Staff.toStaffDetails(): StaffDetails = StaffDetails(
        "JoeBloggs",
        code,
        id,
        Human(getForenames(), surname),
        teams.map { it.toTeam() },
        probationArea.toProbationArea(false),
        grade?.keyValueOf()
    )
}