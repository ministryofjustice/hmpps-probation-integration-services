package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.offence.Offence
import uk.gov.justice.digital.hmpps.api.model.offence.Offences
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

class OffenceIntegrationTest : IntegrationTestBase() {

    @Test
    fun `unauthorized status returned`() {
        mockMvc.get("/sentence/X123456/offences/1")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `person does not exist`() {

        val result = mockMvc.get("/sentence/X123456/offences/1") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn();

        assertEquals(
            "Person with crn of X123456 not found",
            result.resolvedException!!.message
        )
    }

    @Test
    fun `no active events`() {
        val name = Name(
            PersonDetailsGenerator.PERSONAL_DETAILS.forename,
            PersonDetailsGenerator.PERSONAL_DETAILS.secondName,
            PersonDetailsGenerator.PERSONAL_DETAILS.surname
        )

        val expected = Offences(
            name
        )

        val response = mockMvc
            .get("/sentence/${PersonDetailsGenerator.PERSONAL_DETAILS.crn}/offences/1") { withToken() }
            .andExpect { status { isOk() } }
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
            .get("/sentence/${PersonGenerator.OVERVIEW.crn}/offences/${PersonGenerator.EVENT_1.eventNumber}") {
                withToken()
            }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<Offences>()

        assertEquals(expected, response)
    }
}