package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.PERSON
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `get community manager`() {
        mockMvc
            .perform(get("/probation-case/${PERSON.prisonerId}/community-manager").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("firstName", equalTo("Test")))
            .andExpect(jsonPath("lastName", equalTo("User")))
            .andExpect(jsonPath("email", equalTo("test@example.com")))
    }
}
