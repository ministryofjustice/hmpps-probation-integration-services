package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.KeyValue
import uk.gov.justice.digital.hmpps.api.model.conviction.ConvictionRequirements
import uk.gov.justice.digital.hmpps.api.model.conviction.Requirement
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.RequirementsGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class RequirementsByEventIdIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `unauthorized status returned`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        mockMvc
            .perform(get("/probation-case/$crn/convictions/123/requirements"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `probation record not found`() {
        mockMvc
            .perform(get("/probation-case/A123456/convictions/123/requirements").withToken())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Person with crn of A123456 not found"))
    }

    @Test
    fun `sentence not found`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn

        mockMvc
            .perform(get("/probation-case/$crn/convictions/3/requirements").withToken())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Conviction with convictionId 3 not found"))
    }

    @Test
    fun `return list of requirements`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        val event = SentenceGenerator.CURRENTLY_MANAGED

        val requirement = RequirementsGenerator.ACTIVE_REQ
        val expectedResponse = ConvictionRequirements(
            listOf(
                Requirement(
                    requirement.id,
                    requirement.notes,
                    requirement.commencementDate,
                    requirement.startDate,
                    requirement.terminationDate,
                    requirement.expectedStartDate,
                    requirement.expectedEndDate,
                    requirement.createdDatetime,
                    requirement.active,
                    requirement.subCategory?.let { KeyValue(it.code, it.description) },
                    requirement.mainCategory?.let { KeyValue(it.code, it.description) },
                    requirement.adMainCategory?.let { KeyValue(it.code, it.description) },
                    requirement.adSubCategory?.let { KeyValue(it.code, it.description) },
                    requirement.terminationReason?.let { KeyValue(it.code, it.description) },
                    requirement.length,
                    requirement.mainCategory?.let { it.units?.description },
                    restrictive = false,
                    softDeleted = false,
                    rarCount = requirement.rarCount
                )
            )
        )

        val response = mockMvc
            .perform(get("/probation-case/$crn/convictions/${event.id}/requirements").withToken())
            .andExpect(status().isOk)
            .andDo(print())
            .andReturn().response.contentAsJson<ConvictionRequirements>()

        assertEquals(expectedResponse, response)
    }

    @Test
    fun `return empty list of requirements`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        val event = SentenceGenerator.INACTIVE_EVENT

        mockMvc
            .perform(get("/probation-case/$crn/convictions/${event.id}/requirements").withToken())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.requirements").isEmpty)
    }

    @ParameterizedTest
    @CsvSource(
        "activeOnly=false,false,false",
        "excludeSoftDeleted=false,true,true",
        "activeOnly=false&&excludeSoftDeleted=false,false,true"
    )
    fun `return list based on request parameters`(requestParams: String, active: Boolean, deleted: Boolean) {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        val event = SentenceGenerator.CURRENTLY_MANAGED

        mockMvc
            .perform(get("/probation-case/$crn/convictions/${event.id}/requirements?$requestParams").withToken())
            .andExpect(status().isOk)
            .andDo(print())
            .andExpect(jsonPath("$.requirements.length()").value(1))
            .andExpect(jsonPath("$.requirements[0].active").value(active))
            .andExpect(jsonPath("$.requirements[0].softDeleted").value(deleted))
    }
}