package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print

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
            .perform(get("/probation-case/A123456/convictions/123/nsis")
                .param("nsiCodes", "[]")
                .withToken())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Person with crn of A123456 not found"))
    }

}