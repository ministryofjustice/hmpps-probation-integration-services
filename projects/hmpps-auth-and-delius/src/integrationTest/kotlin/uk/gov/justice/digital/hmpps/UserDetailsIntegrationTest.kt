package uk.gov.justice.digital.hmpps

//import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator.TEST_USER
import uk.gov.justice.digital.hmpps.model.UserDetails
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class UserDetailsIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `missing user returns 404`() {
        mockMvc.get("/user/does.not.exist") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `get user`() {
        mockMvc.get("/user/test.user") { withToken() }
            .andExpect { status { isOk() } }
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
    fun `calling user by id without userId returns 400`() {
        mockMvc.get("/user") { withToken() }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `missing user by id returns 404`() {
        mockMvc.get("/user/details/99999") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `get user by id`() {
        mockMvc.get("/user/details/${TEST_USER.id}") { withToken() }
            .andExpect { status { isOk() } }
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
        mockMvc.get("/user?email=test.user@example.com") { withToken() }
            .andExpect { status { isOk() } }
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
