package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@SpringBootTest
@AutoConfigureMockMvc
internal class UserIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @Test
    fun `get user details`() {
        val user = UserGenerator.USER_DETAILS
        mockMvc.perform(get("/user/${user.username.lowercase()}").withOAuth2Token(wireMockserver))
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.username", equalTo("TestUser")))
            .andExpect(jsonPath("$.staffCode", equalTo("TEST002")))
            .andExpect(jsonPath("$.email", equalTo("test@example.com")))
            .andExpect(jsonPath("$.homeArea.code", equalTo("TST")))
            .andExpect(jsonPath("$.homeArea.name", equalTo("Provider description")))
    }

    @Test
    fun `case has no access limitations`() {
        val user = UserGenerator.TEST_USER1
        val person = PersonGenerator.NO_ACCESS_LIMITATIONS

        mockMvc.perform(get("/user/${user.username}/access/${person.crn}").withOAuth2Token(wireMockserver))
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.userExcluded", equalTo(false)))
            .andExpect(jsonPath("$.exclusionMessage").doesNotExist())
            .andExpect(jsonPath("$.userRestricted", equalTo(false)))
            .andExpect(jsonPath("$.restrictionMessage").doesNotExist())
    }

    @Test
    fun `user is excluded from case`() {
        val user = UserGenerator.TEST_USER1
        val person = PersonGenerator.EXCLUDED

        mockMvc.perform(get("/user/${user.username}/access/${person.crn}").withOAuth2Token(wireMockserver))
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.userExcluded", equalTo(true)))
            .andExpect(jsonPath("$.exclusionMessage", equalTo(person.exclusionMessage)))
            .andExpect(jsonPath("$.userRestricted", equalTo(false)))
            .andExpect(jsonPath("$.restrictionMessage").doesNotExist())
    }

    @Test
    fun `case is restricted to user`() {
        val user = UserGenerator.TEST_USER1
        val person = PersonGenerator.RESTRICTED

        mockMvc.perform(get("/user/${user.username}/access/${person.crn}").withOAuth2Token(wireMockserver))
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.userExcluded", equalTo(false)))
            .andExpect(jsonPath("$.exclusionMessage").doesNotExist())
            .andExpect(jsonPath("$.userRestricted", equalTo(false)))
            .andExpect(jsonPath("$.restrictionMessage").doesNotExist())
    }

    @Test
    fun `case is restricted to a different user`() {
        val user = UserGenerator.TEST_USER2
        val person = PersonGenerator.RESTRICTED

        mockMvc.perform(get("/user/${user.username}/access/${person.crn}").withOAuth2Token(wireMockserver))
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.userExcluded", equalTo(false)))
            .andExpect(jsonPath("$.exclusionMessage").doesNotExist())
            .andExpect(jsonPath("$.userRestricted", equalTo(true)))
            .andExpect(jsonPath("$.restrictionMessage", equalTo(person.restrictionMessage)))
    }

    @Test
    fun `case does not exist`() {
        val user = UserGenerator.TEST_USER1

        mockMvc.perform(get("/user/${user.username}/access/NOTFOUND").withOAuth2Token(wireMockserver))
            .andExpect(status().isNotFound)
    }
}
