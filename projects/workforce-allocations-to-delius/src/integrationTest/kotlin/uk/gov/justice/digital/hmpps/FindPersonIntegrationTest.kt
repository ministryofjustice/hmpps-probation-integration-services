package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.CaseType
import uk.gov.justice.digital.hmpps.api.model.Person
import uk.gov.justice.digital.hmpps.api.model.name
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class FindPersonIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @Test
    fun `get person unauthorised`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/person/X123456?type=CRN")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `get person no matching crn`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/person/Z999999?type=CRN")
                .withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `get person by crn`() {
        val wanted = PersonGenerator.DEFAULT
        val res = mockMvc.perform(
            MockMvcRequestBuilders.get("/person/X123456?type=CRN")
                .withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andReturn().response.contentAsString

        val person = objectMapper.readValue<Person>(res)
        assertThat(person, equalTo(Person(wanted.crn, wanted.name(), CaseType.CUSTODY)))
    }

    @Test
    fun `get person by noms`() {
        val wanted = PersonGenerator.DEFAULT
        val res = mockMvc.perform(
            MockMvcRequestBuilders.get("/person/A1234YZ?type=NOMS")
                .withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andReturn().response.contentAsString

        val person = objectMapper.readValue<Person>(res)
        assertThat(person, equalTo(Person(wanted.crn, wanted.name(), CaseType.CUSTODY)))
    }
}