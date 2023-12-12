package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class StaffActiveCasesTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `successful response`() {
        val staff = StaffGenerator.DEFAULT
        val person = PersonGenerator.DEFAULT
        mockMvc.perform(
            MockMvcRequestBuilders.post("/staff/${StaffGenerator.DEFAULT.code}/active-cases")
                .withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listOf(person.crn)))
        )
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.code").value(staff.code))
            .andExpect(jsonPath("$.name.forename").value(staff.forename))
            .andExpect(jsonPath("$.name.surname").value(staff.surname))
            .andExpect(jsonPath("$.grade").value("PSO"))
            .andExpect(jsonPath("$.cases[0].crn").value(person.crn))
            .andExpect(jsonPath("$.cases[0].name.forename").value(person.forename))
            .andExpect(jsonPath("$.cases[0].name.surname").value(person.surname))
            .andExpect(jsonPath("$.cases[0].type").value("CUSTODY"))
    }
}
