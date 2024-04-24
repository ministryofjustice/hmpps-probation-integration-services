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
import uk.gov.justice.digital.hmpps.api.model.offence.Offences
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
            .perform(MockMvcRequestBuilders.get("/sentence/X123456/offences/1"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `no additional offences`() {
        val name = Name(
            PersonDetailsGenerator.PERSONAL_DETAILS.forename,
            PersonDetailsGenerator.PERSONAL_DETAILS.secondName,
            PersonDetailsGenerator.PERSONAL_DETAILS.surname
        )
        val expected = Offences(name, null, listOf())
        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OFFENDER_WITHOUT_EVENTS.crn}/offences/1")
                    .withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<Offences>()

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
            PersonGenerator.MAIN_OFFENCE_1.offence.code,
            PersonGenerator.MAIN_OFFENCE_1.date,
            1,
            PersonGenerator.EVENT_1.notes
        )

        val additionalOffence1 = Offence(
            PersonGenerator.ADDITIONAL_OFFENCE_1.offence.description,
            PersonGenerator.ADDITIONAL_OFFENCE_1.offence.category,
            PersonGenerator.ADDITIONAL_OFFENCE_1.offence.code,
            PersonGenerator.ADDITIONAL_OFFENCE_1.date,
            1,
            null
        )

        val additionalOffence2 = Offence(
            PersonGenerator.ADDITIONAL_OFFENCE_2.offence.description,
            PersonGenerator.ADDITIONAL_OFFENCE_2.offence.category,
            PersonGenerator.ADDITIONAL_OFFENCE_2.offence.code,
            PersonGenerator.ADDITIONAL_OFFENCE_2.date,
            1,
            null
        )

        val expected = Offences(
            name,
            mainOffence,
            listOf(additionalOffence1, additionalOffence2)
        )
        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OVERVIEW.crn}/offences/${PersonGenerator.EVENT_1.eventNumber}")
                    .withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<Offences>()

        assertEquals(expected, response)
    }
}