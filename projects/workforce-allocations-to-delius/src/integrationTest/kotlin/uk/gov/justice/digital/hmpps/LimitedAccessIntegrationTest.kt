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
import uk.gov.justice.digital.hmpps.api.model.UserAccess
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LimitedAccessIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `limited access controls are correctly returned`() {
        val res = mockMvc.perform(
            MockMvcRequestBuilders.post("/user/${UserGenerator.LIMITED_ACCESS_USER.username}/access-controls")
                .withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        listOf(
                            PersonGenerator.EXCLUSION.crn,
                            PersonGenerator.RESTRICTION.crn,
                            PersonGenerator.DEFAULT.crn
                        )
                    )
                )
        ).andReturn().response.contentAsString

        val result = objectMapper.readValue<Map<String, UserAccess>>(res)
        assertThat(
            result[PersonGenerator.EXCLUSION.crn],
            equalTo(
                UserAccess(
                    userExcluded = true,
                    userRestricted = false,
                    exclusionMessage = PersonGenerator.EXCLUSION.exclusionMessage
                )
            )
        )
        assertThat(
            result[PersonGenerator.RESTRICTION.crn],
            equalTo(
                UserAccess(
                    userExcluded = false,
                    userRestricted = true,
                    restrictionMessage = PersonGenerator.RESTRICTION.restrictionMessage
                )
            )
        )
        assertThat(
            result[PersonGenerator.DEFAULT.crn],
            equalTo(
                UserAccess(
                    userExcluded = false,
                    userRestricted = false
                )
            )
        )
    }
}
