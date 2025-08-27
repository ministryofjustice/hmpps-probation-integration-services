package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator.BREACH_NOTICE_ID
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator.PSS_BREACH_NOTICE_ID
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.DEFAULT_DISPOSAL
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator.PSS_REQUIREMENT
import uk.gov.justice.digital.hmpps.model.RequirementResponse
import uk.gov.justice.digital.hmpps.service.toModel
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

internal class RequirementsIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `can retrieve requirements`() {
        val response = mockMvc
            .perform(get("/requirements/$BREACH_NOTICE_ID").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<RequirementResponse>()

        val requirements = requirementRepository.findAllByDisposalId(DEFAULT_DISPOSAL.id)
        assertThat(requirements).isNotEmpty
        assertThat(response).isEqualTo(RequirementResponse(requirements = requirements.map { it.toModel() }))
    }

    @Test
    fun `can retrieve pss requirements`() {
        val response = mockMvc
            .perform(get("/requirements/$PSS_BREACH_NOTICE_ID").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<RequirementResponse>()

        assertThat(response).isEqualTo(RequirementResponse(requirements = listOf(PSS_REQUIREMENT.toModel())))
    }
}