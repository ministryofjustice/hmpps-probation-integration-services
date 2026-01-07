package uk.gov.justice.digital.hmpps

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.service.CaseAccess
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
}
