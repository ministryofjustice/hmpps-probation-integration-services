package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.Person
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PERSON1
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class GetPersonByCRNIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    val crn = PERSON1.crn

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(get("/probation-case/$crn"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `API call probation record not found`() {
        mockMvc
            .perform(get("/probation-case/Z123456").withToken())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Person with crn of Z123456 not found"))
    }

    @Test
    fun `API call return person data`() {
        val expectedResponse = Person(Name("Jon", "Harry Fred", "Smith"))

        val response = mockMvc
            .perform(get("/probation-case/$crn").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<Person>()

        assertEquals(expectedResponse, response)
    }
}
