package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.ATTENDED_COMPLIED_CONTACT_OUTCOME
import uk.gov.justice.digital.hmpps.model.CodeDescription
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest
class ReferenceDataIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    internal lateinit var wireMockServer: WireMockServer

    @Test
    fun `can retrieve all selectable unpaid work project types`() {
        val response = mockMvc
            .perform(get("/reference-data/project-types").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<List<CodeDescription>>()

        assertThat(response.size).isEqualTo(2)
    }

    @Test
    fun `can retrieve linked outcomes for a contact type`() {
        val response = mockMvc
            .perform(get("/reference-data/unpaid-work-appointment-outcomes").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<List<CodeDescription>>()

        assertThat(response.size).isEqualTo(1)
        assertThat(response.single()).isEqualTo(
            CodeDescription(
                ATTENDED_COMPLIED_CONTACT_OUTCOME.code,
                ATTENDED_COMPLIED_CONTACT_OUTCOME.description
            )
        )
    }
}