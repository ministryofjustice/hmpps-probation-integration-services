package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.model.PersonExists
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class PersonExistsIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @ParameterizedTest
    @CsvSource(
        "A000001,true",
        "A000002,false",
    )
    fun `check if crn exists in delius`(crn: String, expectedResult: Boolean) {

        val expectedResponse = PersonExists(crn, expectedResult)

        val response = mockMvc
            .perform(get("/exists-in-delius/crn/$crn").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<PersonExists>()

        assertEquals(expectedResponse, response)
    }
}