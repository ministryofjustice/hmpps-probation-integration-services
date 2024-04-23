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
import uk.gov.justice.digital.hmpps.api.model.offence.Offence
import uk.gov.justice.digital.hmpps.api.model.offence.OffenceDetails
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OffenceIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/sentence/X123456/offence-details/1"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `no additional offences`() {
        val name = Name(
            PersonDetailsGenerator.PERSONAL_DETAILS.forename,
            PersonDetailsGenerator.PERSONAL_DETAILS.secondName,
            PersonDetailsGenerator.PERSONAL_DETAILS.surname
        )
        val expected = OffenceDetails(name, null, null, listOf())
        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OFFENDER_WITHOUT_EVENTS.crn}/offence-details/1")
                    .withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<OffenceDetails>()

        assertEquals(expected, response)
    }

    @Test
    fun `return additional offences`() {
        val name = Name(
            PersonGenerator.OVERVIEW.forename,
            PersonGenerator.OVERVIEW.secondName,
            PersonGenerator.OVERVIEW.surname
        )

        val mainOffence = Offence(
            PersonGenerator.MAIN_OFFENCE_1.offence.description,
            PersonGenerator.MAIN_OFFENCE_1.offence.category,
            PersonGenerator.MAIN_OFFENCE_1.date
        )

        val additionalOffence1 = Offence(
            PersonGenerator.ADDITIONAL_OFFENCE_1.offence.description,
            PersonGenerator.ADDITIONAL_OFFENCE_1.offence.category,
            PersonGenerator.ADDITIONAL_OFFENCE_1.date
        )

        val additionalOffence2 = Offence(
            PersonGenerator.ADDITIONAL_OFFENCE_2.offence.description,
            PersonGenerator.ADDITIONAL_OFFENCE_2.offence.category,
            PersonGenerator.ADDITIONAL_OFFENCE_2.date
        )

        val expected = OffenceDetails(
            name,
            mainOffence,
            PersonGenerator.EVENT_1.notes,
            listOf(additionalOffence1, additionalOffence2)
        )
        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OVERVIEW.crn}/offence-details/${PersonGenerator.EVENT_1.eventNumber}")
                    .withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<OffenceDetails>()

        assertEquals(expected, response)
    }
}