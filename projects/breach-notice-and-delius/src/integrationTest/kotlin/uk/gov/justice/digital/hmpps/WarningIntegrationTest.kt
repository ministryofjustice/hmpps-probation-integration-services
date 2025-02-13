package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.WarningGenerator.WARNING_TYPES
import uk.gov.justice.digital.hmpps.model.CodedDescription
import uk.gov.justice.digital.hmpps.model.WarningTypes
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class WarningIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `can retrieve all warning types`() {
        val response = mockMvc
            .perform(get("/warning-types").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<WarningTypes>()

        assertThat(response.content)
            .containsExactlyElementsOf(
                WARNING_TYPES.filter { it.selectable }
                    .map { CodedDescription(it.code, it.description) }
                    .sortedBy { it.description }
            )
    }
}