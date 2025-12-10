package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@SpringBootTest
@AutoConfigureMockMvc
internal class ProviderIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `get provider by code`() {
        val provider = PersonGenerator.DEFAULT_PROVIDER
        mockMvc.get("/provider/${provider.code}") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.code") { value(equalTo("TST")) }
                jsonPath("$.name") { value(equalTo("Provider description")) }
            }
    }

    @Test
    fun `get providers`() {
        mockMvc.get("/provider") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.size()") { value(equalTo(1)) }
                jsonPath("$[0].code") { value(equalTo("TST")) }
                jsonPath("$[0].name") { value(equalTo("Provider description")) }
            }
    }
}
