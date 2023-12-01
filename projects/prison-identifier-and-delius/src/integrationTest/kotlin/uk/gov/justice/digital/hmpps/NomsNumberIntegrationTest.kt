package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.sevice.model.NomsUpdates

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class NomsNumberIntegrationTest {

    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `API call retuns not found in delius`() {
        val crn = "ZZZ"

        val result = mockMvc
            .perform(
                post("/person/populate-noms-number").withOAuth2Token(wireMockServer)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(listOf(crn)))
            )
            .andExpect(status().is2xxSuccessful).andReturn()

        val detailResponse = objectMapper.readValue(result.response.contentAsString, NomsUpdates::class.java)
        Assertions.assertThat(detailResponse.personMatches.first().matchDetail!!.message).isEqualTo("CRN not found in Delius")
    }

    @Test
    fun `API call retuns Noms number already in delius`() {
        val crn = PersonGenerator.PERSON_WITH_NOMS.crn

        val result = mockMvc
            .perform(
                post("/person/populate-noms-number").withOAuth2Token(wireMockServer)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(listOf(crn)))
            )
            .andExpect(status().is2xxSuccessful).andReturn()

        val detailResponse = objectMapper.readValue(result.response.contentAsString, NomsUpdates::class.java)
        Assertions.assertThat(detailResponse.personMatches.first().matchDetail!!.message).isEqualTo("Noms number already in Delius")
    }

    @Test
    fun `API call retuns single match via prison search api`() {
        val crn = PersonGenerator.PERSON_WITH_NO_NOMS.crn

        val result = mockMvc
            .perform(
                post("/person/populate-noms-number").withOAuth2Token(wireMockServer)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(listOf(crn)))
            )
            .andExpect(status().is2xxSuccessful).andReturn()

        val detailResponse = objectMapper.readValue(result.response.contentAsString, NomsUpdates::class.java)
        Assertions.assertThat(detailResponse.personMatches.first().matchDetail!!.message).isEqualTo("Found a single match in prison search api")
    }

    @Test
    fun `API call retuns a single match from multiple matches found in prison search api`() {
        val crn = PersonGenerator.PERSON_WITH_MULTI_MATCH.crn

        val result = mockMvc
            .perform(
                post("/person/populate-noms-number").withOAuth2Token(wireMockServer)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(listOf(crn)))
            )
            .andExpect(status().is2xxSuccessful).andReturn()

        val detailResponse = objectMapper.readValue(result.response.contentAsString, NomsUpdates::class.java)
        Assertions.assertThat(detailResponse.personMatches.first().matchDetail!!.message).isEqualTo("Found a single match in prison search api and matching criteria.")
    }

    @Test
    fun `API call cant determine a match from multiple matches found in prison search api`() {
        val crn = PersonGenerator.PERSON_WITH_NO_MATCH.crn

        val result = mockMvc
            .perform(
                post("/person/populate-noms-number").withOAuth2Token(wireMockServer)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(listOf(crn)))
            )
            .andExpect(status().is2xxSuccessful).andReturn()

        val detailResponse = objectMapper.readValue(result.response.contentAsString, NomsUpdates::class.java)
        Assertions.assertThat(detailResponse.personMatches.first().matchDetail!!.message).isEqualTo("Unable to find a unique match using matching criteria.")
    }
}
