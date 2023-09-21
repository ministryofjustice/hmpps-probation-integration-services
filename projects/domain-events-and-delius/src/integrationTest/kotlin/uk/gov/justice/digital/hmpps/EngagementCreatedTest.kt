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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.service.Engagement
import uk.gov.justice.digital.hmpps.service.Identifiers
import uk.gov.justice.digital.hmpps.service.Name

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class EngagementCreatedTest {
    @Autowired
    internal lateinit var mockMvc: MockMvc

    @Autowired
    internal lateinit var wireMockServer: WireMockServer

    @Autowired
    internal lateinit var objectMapper: ObjectMapper

    @Test
    fun `engagement details returned from detail url`() {
        val person = PersonGenerator.ENGAGEMENT_CREATED
        val response = mockMvc
            .perform(get("/probation-case.engagement.created/${person.crn}").withOAuth2Token(wireMockServer))
            .andExpect(MockMvcResultMatchers.status().is2xxSuccessful).andReturn().response.contentAsString

        val engagement = objectMapper.readValue<Engagement>(response)
        assertThat(
            engagement,
            equalTo(
                Engagement(
                    Identifiers(person.crn, person.pnc),
                    Name(person.forename, person.surname, listOf()),
                    person.dateOfBirth
                )
            )
        )
    }
}