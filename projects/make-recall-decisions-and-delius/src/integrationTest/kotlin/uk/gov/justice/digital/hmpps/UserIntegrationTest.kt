package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@SpringBootTest
@AutoConfigureMockMvc
internal class UserIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `get user details`() {
        val user = UserGenerator.USER_DETAILS
        mockMvc.get("/user/${user.username.lowercase()}") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.username") { value(equalTo("TestUser")) }
                jsonPath("$.staffCode") { value(equalTo("TEST002")) }
                jsonPath("$.email") { value(equalTo("test@example.com")) }
                jsonPath("$.homeArea.code") { value(equalTo("TST")) }
                jsonPath("$.homeArea.name") { value(equalTo("Provider description")) }
            }
    }

    @Test
    fun `case has no access limitations`() {
        val user = UserGenerator.TEST_USER1
        val person = PersonGenerator.NO_ACCESS_LIMITATIONS

        mockMvc.get("/user/${user.username}/access/${person.crn}") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.userExcluded") { value(equalTo(false)) }
                jsonPath("$.exclusionMessage") { doesNotExist() }
                jsonPath("$.userRestricted") { value(equalTo(false)) }
                jsonPath("$.restrictionMessage") { doesNotExist() }
            }
    }

    @Test
    fun `user is excluded from case`() {
        val user = UserGenerator.TEST_USER1
        val person = PersonGenerator.EXCLUDED

        mockMvc.get("/user/${user.username}/access/${person.crn}") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.userExcluded") { value(equalTo(true)) }
                jsonPath("$.exclusionMessage") { value(equalTo(person.exclusionMessage)) }
                jsonPath("$.userRestricted") { value(equalTo(false)) }
                jsonPath("$.restrictionMessage") { doesNotExist() }
            }
    }

    @Test
    fun `case is restricted to user`() {
        val user = UserGenerator.TEST_USER1
        val person = PersonGenerator.RESTRICTED

        mockMvc.get("/user/${user.username}/access/${person.crn}") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.userExcluded") { value(equalTo(false)) }
                jsonPath("$.exclusionMessage") { doesNotExist() }
                jsonPath("$.userRestricted") { value(equalTo(false)) }
                jsonPath("$.restrictionMessage") { doesNotExist() }
            }
    }

    @Test
    fun `case is restricted to a different user`() {
        val user = UserGenerator.TEST_USER2
        val person = PersonGenerator.RESTRICTED

        mockMvc.get("/user/${user.username}/access/${person.crn}") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.userExcluded") { value(equalTo(false)) }
                jsonPath("$.exclusionMessage") { doesNotExist() }
                jsonPath("$.userRestricted") { value(equalTo(true)) }
                jsonPath("$.restrictionMessage") { value(equalTo(person.restrictionMessage)) }
            }
    }

    @Test
    fun `case does not exist`() {
        val user = UserGenerator.TEST_USER1

        mockMvc.get("/user/${user.username}/access/NOTFOUND") { withToken() }
            .andExpect { status { isNotFound() } }
    }
}
