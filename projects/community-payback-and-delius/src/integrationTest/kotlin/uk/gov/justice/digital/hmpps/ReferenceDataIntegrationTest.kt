package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.ATTENDED_COMPLIED_CONTACT_OUTCOME
import uk.gov.justice.digital.hmpps.model.CodeDescription
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest
class ReferenceDataIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `can retrieve all selectable unpaid work project types`() {
        val response = mockMvc
            .get("/reference-data/project-types") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<List<CodeDescription>>()

        assertThat(response.size).isEqualTo(2)
    }

    @Test
    fun `can retrieve linked outcomes for a contact type`() {
        val response = mockMvc
            .get("/reference-data/unpaid-work-appointment-outcomes") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<List<CodeDescription>>()

        assertThat(response.size).isEqualTo(1)
        assertThat(response.single()).isEqualTo(
            CodeDescription(
                ATTENDED_COMPLIED_CONTACT_OUTCOME.code,
                ATTENDED_COMPLIED_CONTACT_OUTCOME.description
            )
        )
    }

    @Test
    fun `can retrieve non working days`() {
        val response = mockMvc
            .get("/reference-data/non-working-days") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<List<LocalDate>>()

        assertThat(response.size).isEqualTo(2)
        assertThat(response).containsExactlyInAnyOrder(
            LocalDate.of(2025, 12, 25),
            LocalDate.of(2026, 1, 1)
        )
    }
}
