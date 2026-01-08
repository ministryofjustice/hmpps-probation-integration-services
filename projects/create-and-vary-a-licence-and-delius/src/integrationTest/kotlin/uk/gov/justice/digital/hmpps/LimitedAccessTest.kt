package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.service.CaseAccess
import uk.gov.justice.digital.hmpps.service.UserAccess
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class LimitedAccessTest @Autowired constructor(
    private val mockMvc: MockMvc,
) {
    @Test
    fun `should return 200 and Allow full access`() {
        val url =
            "/users/${LimitedAccessGenerator.LAO_DEFAULT_USER.username}/access/${PersonGenerator.DEFAULT_PERSON.crn}"
        val res = mockMvc.get(url) { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<CaseAccess>()
        assertThat(res.userExcluded, equalTo(false))
        assertThat(res.userRestricted, equalTo(false))
    }

    @Test
    fun `should return 200 and Allow restricted access`() {
        val url =
            "/users/${LimitedAccessGenerator.LAO_RESTRICTED_USER.username}/access/${LimitedAccessGenerator.LAO_RESTRICTION.person.crn}"
        val res = mockMvc.get(url) { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<CaseAccess>()
        assertThat(res.userExcluded, equalTo(false))
        assertThat(res.userRestricted, equalTo(false))
    }

    @Test
    fun `should return 200 and Not Allow restricted access`() {
        val url =
            "/users/${LimitedAccessGenerator.LAO_EXCLUDED_USER.username}/access/${LimitedAccessGenerator.LAO_RESTRICTION.person.crn}"
        val res = mockMvc.get(url) { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<CaseAccess>()
        assertThat(res.userExcluded, equalTo(false))
        assertThat(res.userRestricted, equalTo(true))
    }

    @Test
    fun `should return 200 and Allow excluded access`() {
        val url =
            "/users/${LimitedAccessGenerator.LAO_RESTRICTED_USER.username}/access/${LimitedAccessGenerator.LAO_EXCLUSION.person.crn}"
        val res = mockMvc.get(url) { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<CaseAccess>()
        assertThat(res.userExcluded, equalTo(false))
        assertThat(res.userRestricted, equalTo(false))
    }

    @Test
    fun `should return 200 and Not Allow Excluded access`() {
        val url =
            "/users/${LimitedAccessGenerator.LAO_EXCLUDED_USER.username}/access/${LimitedAccessGenerator.LAO_EXCLUSION.person.crn}"
        val res = mockMvc.get(url) { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<CaseAccess>()
        assertThat(res.userExcluded, equalTo(true))
        assertThat(res.userRestricted, equalTo(false))
    }

    @Test
    fun `should return list of exclusions and restrictions for user given list of CRNS`() {
        val url =
            "/users/${LimitedAccessGenerator.LAO_DEFAULT_USER.username}/access"
        val requestBody = listOf(
            LimitedAccessGenerator.LAO_RESTRICTION.person.crn,
            LimitedAccessGenerator.LAO_EXCLUSION.person.crn,
            PersonGenerator.DEFAULT_PERSON.crn
        )
        val json = jacksonObjectMapper().writeValueAsString(requestBody)
        val res = mockMvc.post(url) {
            withToken()
            content = json
            contentType = MediaType.APPLICATION_JSON
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsString

        val actualResult = jacksonObjectMapper().readValue(res, UserAccess::class.java)
        assertThat(actualResult.access.size, equalTo(requestBody.size))
    }
}
