package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator.TEST_USER
import uk.gov.justice.digital.hmpps.model.UserDetails
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class UserDetailsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `missing user returns 404`() {
        mockMvc.perform(get("/user/does.not.exist").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `get user`() {
        mockMvc.perform(get("/user/test.user").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpectJson(
                UserDetails(
                    userId = TEST_USER.id,
                    username = "test.user",
                    firstName = "Test",
                    surname = "User",
                    email = "test.user@example.com",
                    enabled = true,
                    roles = listOf("ABC001", "ABC002")
                )
            )
    }

    @Test
    fun `search by email`() {
        mockMvc.perform(get("/user?email=test.user@example.com").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpectJson(
                listOf(
                    UserDetails(
                        userId = TEST_USER.id,
                        username = "test.user",
                        firstName = "Test",
                        surname = "User",
                        email = "test.user@example.com",
                        enabled = true,
                        roles = listOf("ABC001", "ABC002")
                    )
                )
            )
    }

    fun <T> ResultActions.andExpectJson(obj: T) = this.andExpect(content().json(objectMapper.writeValueAsString(obj)))
}
