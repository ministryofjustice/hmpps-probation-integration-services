package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.CaseIdentifier
import uk.gov.justice.digital.hmpps.api.model.ManagedCases
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserResourceTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `correctly returns cases managed by a given user`() {
        val res = mockMvc.perform(
            MockMvcRequestBuilders.get("/users/john-smith/managed-cases")
                .withOAuth2Token(wireMockServer)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful).andReturn().response.contentAsString

        val managedCases = objectMapper.readValue<ManagedCases>(res)
        assertThat(managedCases.managedCases.size, equalTo(2))
        assertThat(managedCases.managedCases, hasItem(CaseIdentifier(PersonGenerator.COMMUNITY_RESPONSIBLE.crn)))
        assertThat(managedCases.managedCases, hasItem(CaseIdentifier(PersonGenerator.COMMUNITY_NOT_RESPONSIBLE.crn)))
    }
}
