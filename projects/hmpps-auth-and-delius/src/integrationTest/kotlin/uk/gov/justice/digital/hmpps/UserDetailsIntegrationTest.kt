package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator.TEST_USER
import uk.gov.justice.digital.hmpps.model.UserDetails
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class UserDetailsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `missing user returns 404`() {
        mockMvc.perform(get("/user/does.not.exist").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `get user`() {
        mockMvc.perform(get("/user/test.user").withToken())
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
        mockMvc.perform(get("/user?email=test.user@example.com").withToken())
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
}
