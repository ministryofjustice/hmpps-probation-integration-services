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
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.service.CaseAccess
import uk.gov.justice.digital.hmpps.service.UserAccess

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class LimitedAccessTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `limited access controls are correctly returned with username`() {
        val res = mockMvc.perform(
            MockMvcRequestBuilders.post("/users/access?username=${LimitedAccessGenerator.LIMITED_ACCESS_USER.username}")
                .withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        listOf(
                            LimitedAccessGenerator.EXCLUDED_CASE.crn,
                            LimitedAccessGenerator.RESTRICTED_CASE.crn,
                            LimitedAccessGenerator.UNLIMITED_ACCESS.crn
                        )
                    )
                )
        ).andReturn().response.contentAsString

        validateResults(res)
    }

    @Test
    fun `limited access controls are correctly returned without username`() {
        val res = mockMvc.perform(
            MockMvcRequestBuilders.post("/users/access")
                .withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        listOf(
                            LimitedAccessGenerator.EXCLUDED_CASE.crn,
                            LimitedAccessGenerator.RESTRICTED_CASE.crn,
                            LimitedAccessGenerator.UNLIMITED_ACCESS.crn
                        )
                    )
                )
        ).andReturn().response.contentAsString

        validateResults(res)
    }

    private fun validateResults(res: String) {
        val result = objectMapper.readValue<UserAccess>(res)
        assertThat(
            result.access.first { it.crn == LimitedAccessGenerator.EXCLUDED_CASE.crn },
            equalTo(
                CaseAccess(
                    LimitedAccessGenerator.EXCLUDED_CASE.crn,
                    userExcluded = true,
                    userRestricted = false,
                    exclusionMessage = LimitedAccessGenerator.EXCLUDED_CASE.exclusionMessage
                )
            )
        )
        assertThat(
            result.access.first { it.crn == LimitedAccessGenerator.RESTRICTED_CASE.crn },
            equalTo(
                CaseAccess(
                    LimitedAccessGenerator.RESTRICTED_CASE.crn,
                    userExcluded = false,
                    userRestricted = true,
                    restrictionMessage = LimitedAccessGenerator.RESTRICTED_CASE.restrictionMessage
                )
            )
        )
        assertThat(
            result.access.first { it.crn == LimitedAccessGenerator.UNLIMITED_ACCESS.crn },
            equalTo(
                CaseAccess(
                    LimitedAccessGenerator.UNLIMITED_ACCESS.crn,
                    userExcluded = false,
                    userRestricted = false
                )
            )
        )
    }

    @Test
    fun `limited access controls are correctly returned with full access`() {
        val res = mockMvc.perform(
            MockMvcRequestBuilders.post("/users/access?username=${LimitedAccessGenerator.FULL_ACCESS_USER.username}")
                .withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        listOf(
                            LimitedAccessGenerator.EXCLUDED_CASE.crn,
                            LimitedAccessGenerator.RESTRICTED_CASE.crn,
                            LimitedAccessGenerator.UNLIMITED_ACCESS.crn
                        )
                    )
                )
        ).andReturn().response.contentAsString

        val result = objectMapper.readValue<UserAccess>(res)
        assertThat(
            result.access.first { it.crn == LimitedAccessGenerator.EXCLUDED_CASE.crn },
            equalTo(
                CaseAccess(
                    LimitedAccessGenerator.EXCLUDED_CASE.crn,
                    userExcluded = false,
                    userRestricted = false
                )
            )
        )
        assertThat(
            result.access.first { it.crn == LimitedAccessGenerator.RESTRICTED_CASE.crn },
            equalTo(
                CaseAccess(
                    LimitedAccessGenerator.RESTRICTED_CASE.crn,
                    userExcluded = false,
                    userRestricted = false
                )
            )
        )
        assertThat(
            result.access.first { it.crn == LimitedAccessGenerator.UNLIMITED_ACCESS.crn },
            equalTo(
                CaseAccess(
                    LimitedAccessGenerator.UNLIMITED_ACCESS.crn,
                    userExcluded = false,
                    userRestricted = false
                )
            )
        )
    }
}
