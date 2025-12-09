package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.model.PersonExists
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

internal class PersonExistsIntegrationTest : BaseIntegrationTest() {

    @ParameterizedTest
    @CsvSource(
        "A000001,true",
        "A000002,false",
    )
    fun `check if crn exists in delius`(crn: String, expectedResult: Boolean) {

        val expectedResponse = PersonExists(crn, expectedResult)

        val response = mockMvc
            .get("/exists-in-delius/crn/$crn") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<PersonExists>()

        assertEquals(expectedResponse, response)
    }
}