package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.overview.Order
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.INACTIVE_EVENT_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.INACTIVE_ORDER_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.MAIN_OFFENCE_3
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OFFENCE_1
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

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

        mockMvc
            .perform(MockMvcRequestBuilders.get("/sentence/X123456/previous-orders/1"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }



    @Test
    fun `no previous orders`() {
        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonDetailsGenerator.PERSONAL_DETAILS.crn}/previous-orders")
                    .withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<PreviousOrderHistory>()

        val expected = PreviousOrderHistory(Name("Caroline", "Louise", "Bloggs"), listOf())

        assertEquals(expected, response)
    }

    @Test
    fun `no previous orders by crn and event number`() {
        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonDetailsGenerator.PERSONAL_DETAILS.crn}/previous-orders/1")
                    .withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<PreviousOrderHistory>()

        val expected = PreviousOrderHistory(Name("Caroline", "Louise", "Bloggs"), listOf())

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
            Name("Forename", "Middle1", "Surname"),
            listOf(
                PreviousOrder(
                    "Default Sentence Type (7 Months)",
                    "Burglary, other than a dwelling - 03000",
                    LocalDate.now().minusDays(7)
                ),
                PreviousOrder("Default Sentence Type (25 Years)", "Murder", LocalDate.now().minusDays(8))
            )
        )

        assertEquals(expected, response)
    }

    @Test
    fun `return previous orders by crn and event id`() {
        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OVERVIEW.crn}/previous-orders/${INACTIVE_EVENT_1.eventNumber}").withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<PreviousOrderInformation>()

        val expected = PreviousOrderInformation(
            Name("Forename", "Middle1", "Surname"),
            "Default Sentence Type (25 Years)",
            Sentence(
                OffenceDetails(
                    INACTIVE_EVENT_1.eventNumber,
                    Offence(OFFENCE_1.description, 1),
                    MAIN_OFFENCE_3.date,
                    INACTIVE_EVENT_1.notes,
                    listOf()
                ),
                Conviction(null, null, null, listOf()),
                Order(INACTIVE_ORDER_1.type.description, INACTIVE_ORDER_1.length, startDate = INACTIVE_ORDER_1.date, breaches = 0, endDate = null),
                courtDocuments = listOf(),
                requirements = null,
                licenceConditions = null
            )

        )

        assertEquals(expected, response)
    }
}