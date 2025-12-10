package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.overview.Order
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.INACTIVE_EVENT_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.INACTIVE_EVENT_2
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.INACTIVE_EVENT_3
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.INACTIVE_EVENT_NO_TIME_UNIT
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.INACTIVE_ORDER_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.MAIN_OFFENCE_3
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OFFENCE_1
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

class OrderIntegrationTest : IntegrationTestBase() {

    @Test
    fun `unauthorized status returned`() {
        mockMvc.get("/sentence/X123456/previous-orders")
            .andExpect { status { isUnauthorized() } }

        mockMvc.get("/sentence/X123456/previous-orders/1")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `no previous orders`() {
        val response =
            mockMvc.get("/sentence/${PersonDetailsGenerator.PERSONAL_DETAILS.crn}/previous-orders") { withToken() }
                .andExpect { status { isOk() } }
                .andReturn().response.contentAsJson<PreviousOrderHistory>()

        val expected = PreviousOrderHistory(Name("Caroline", "Louise", "Bloggs"), listOf())

        assertEquals(expected, response)
    }

    @Test
    fun `no previous orders by crn and event number`() {
        val response =
            mockMvc.get("/sentence/${PersonDetailsGenerator.PERSONAL_DETAILS.crn}/previous-orders/1") { withToken() }
                .andExpect { status { isOk() } }
                .andReturn().response.contentAsJson<PreviousOrderHistory>()

        val expected = PreviousOrderHistory(Name("Caroline", "Louise", "Bloggs"), listOf())

        assertEquals(expected, response)
    }

    @Test
    fun `return previous orders`() {
        val response = mockMvc.get("/sentence/${PersonGenerator.OVERVIEW.crn}/previous-orders") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<PreviousOrderHistory>()

        val expected = PreviousOrderHistory(
            Name("Forename", "Middle1", "Surname"),
            listOf(
                PreviousOrder(
                    INACTIVE_EVENT_NO_TIME_UNIT.eventNumber,
                    "Default Sentence Type (36 not provided)",
                    "Burglary, other than a dwelling - 03000",
                    LocalDate.now().minusDays(7)
                ),
                PreviousOrder(
                    INACTIVE_EVENT_3.eventNumber,
                    "Default Sentence Type",
                    "Burglary, other than a dwelling - 03000",
                    LocalDate.now().minusDays(7)
                ),
                PreviousOrder(
                    INACTIVE_EVENT_2.eventNumber,
                    "Default Sentence Type (7 Months)",
                    "Burglary, other than a dwelling - 03000",
                    LocalDate.now().minusDays(7)
                ),
                PreviousOrder(
                    INACTIVE_EVENT_1.eventNumber,
                    "Default Sentence Type (2 Years)",
                    "Murder",
                    LocalDate.now().minusDays(8)
                )
            )
        )

        assertEquals(expected, response)
    }

    @Test
    fun `return previous orders by crn and event id`() {
        val response =
            mockMvc.get("/sentence/${PersonGenerator.OVERVIEW.crn}/previous-orders/${INACTIVE_EVENT_1.eventNumber}") { withToken() }
                .andExpect { status { isOk() } }
                .andReturn().response.contentAsJson<PreviousOrderInformation>()

        val expected = PreviousOrderInformation(
            Name("Forename", "Middle1", "Surname"),
            "Default Sentence Type (2 Years)",
            Sentence(
                OffenceDetails(
                    INACTIVE_EVENT_1.eventNumber,
                    Offence(OFFENCE_1.description, 1),
                    MAIN_OFFENCE_3.date,
                    INACTIVE_EVENT_1.notes,
                    listOf()
                ),
                Conviction(null, null, null, listOf()),
                Order(
                    description = INACTIVE_ORDER_1.type.description,
                    length = INACTIVE_ORDER_1.length,
                    startDate = INACTIVE_ORDER_1.date,
                    breaches = 0,
                    endDate = null
                ),
                courtDocuments = listOf(),
                requirements = null,
                licenceConditions = null
            )

        )

        assertEquals(expected, response)
    }

    @Test
    fun `return previous order no disposal length`() {
        val response =
            mockMvc.get("/sentence/${PersonGenerator.OVERVIEW.crn}/previous-orders/${INACTIVE_EVENT_3.eventNumber}") {
                withToken()
            }
                .andExpect { status { isOk() } }
                .andReturn().response.contentAsJson<PreviousOrderInformation>()

        assertEquals(response.title, "Default Sentence Type")
    }
}