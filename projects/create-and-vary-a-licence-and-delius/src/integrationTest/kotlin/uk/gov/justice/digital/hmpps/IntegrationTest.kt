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
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.service.asManager

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `returns responsible officer details`() {
        val crn = PersonGenerator.DEFAULT_PERSON.crn

        val res = mockMvc
            .perform(get("/probation-case/$crn/responsible-community-manager").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val manager = objectMapper.readValue<Manager>(res)
        assertThat(
            manager,
            equalTo(
                PersonGenerator.DEFAULT_CM.asManager().copy(username = "john-smith", email = "john.smith@moj.gov.uk")
            )
        )
    }

    @Test
    fun `returns 404 if no crn or community officer`() {
        mockMvc.perform(
            get("/probation-case/Z123456/responsible-community-manager")
                .withOAuth2Token(wireMockServer)
        ).andExpect(status().isNotFound)
    }
}
