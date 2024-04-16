package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.sentence.PreviousOrder
import uk.gov.justice.digital.hmpps.api.model.sentence.PreviousOrderHistory
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/sentence/X123456/previous-orders"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `no previous orders`() {
        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OFFENDER_WITHOUT_EVENTS.crn}/previous-orders")
                    .withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<PreviousOrderHistory>()

        val expected = PreviousOrderHistory(listOf())

        assertEquals(expected, response)
    }

    @Test
    fun `return previous orders`() {
        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OVERVIEW.crn}/previous-orders").withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<PreviousOrderHistory>()

        val expected = PreviousOrderHistory(
            listOf(
                PreviousOrder("Burglary, other than a dwelling - 03000 (7 Months)", "Default Sentence Type"),
                PreviousOrder("Murder (25 Years)", "Default Sentence Type")
            )
        )

        assertEquals(expected, response)
    }
}