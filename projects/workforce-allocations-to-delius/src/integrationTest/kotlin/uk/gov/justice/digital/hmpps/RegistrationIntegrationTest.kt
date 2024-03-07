package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.Colour
import uk.gov.justice.digital.hmpps.api.model.RiskItem
import uk.gov.justice.digital.hmpps.api.model.RiskSummary
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RegistrationIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `test crn with risk flags`() {
        val result = mockMvc.perform(MockMvcRequestBuilders
            .get("/registrations/X123456/flags").withToken())
            .andDo(print())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<RiskSummary>()

        val expected = RiskSummary(
            selfHarm = RiskItem("Rosh", Colour.RED),
            alerts = RiskItem("Alerts", Colour.RED),
            safeguarding = RiskItem("Safeguarding", Colour.RED),
            information = RiskItem("Information", Colour.RED),
            publicProtection = RiskItem("Public Protection", Colour.RED)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `test no registrations`() {
        val result = mockMvc.perform(MockMvcRequestBuilders
            .get("/registrations/N123456/flags").withToken())
            .andDo(print())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<RiskSummary>()

        val expected = RiskSummary()
        assertEquals(expected, result)

    }
}