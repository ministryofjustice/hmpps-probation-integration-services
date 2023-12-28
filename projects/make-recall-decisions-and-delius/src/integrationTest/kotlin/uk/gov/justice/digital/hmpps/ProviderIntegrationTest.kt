package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@SpringBootTest
@AutoConfigureMockMvc
internal class ProviderIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `get provider by code`() {
        val provider = PersonGenerator.DEFAULT_PROVIDER
        mockMvc.perform(get("/provider/${provider.code}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.code", equalTo("TST")))
            .andExpect(jsonPath("$.name", equalTo("Provider description")))
    }

    @Test
    fun `get providers`() {
        mockMvc.perform(get("/provider").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.size()", equalTo(1)))
            .andExpect(jsonPath("$[0].code", equalTo("TST")))
            .andExpect(jsonPath("$[0].name", equalTo("Provider description")))
    }
}
