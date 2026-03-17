package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
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
                jsonPath("$.username") { value("TestUser") }
                jsonPath("$.staffCode") { value("TEST002") }
                jsonPath("$.email") { value("test@example.com") }
                jsonPath("$.homeArea.code") { value("TST") }
                jsonPath("$.homeArea.name") { value("Provider description") }
            }
    }

    @Test
    fun `case has no access limitations`() {
        val user = UserGenerator.TEST_USER1
        val person = PersonGenerator.NO_ACCESS_LIMITATIONS

        mockMvc.get("/user/${user.username}/access/${person.crn}") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.userExcluded") { value(false) }
                jsonPath("$.exclusionMessage") { doesNotExist() }
                jsonPath("$.userRestricted") { value(false) }
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
                jsonPath("$.userExcluded") { value(true) }
                jsonPath("$.exclusionMessage") { value(person.exclusionMessage) }
                jsonPath("$.userRestricted") { value(false) }
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
                jsonPath("$.userExcluded") { value(false) }
                jsonPath("$.exclusionMessage") { doesNotExist() }
                jsonPath("$.userRestricted") { value(false) }
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
                jsonPath("$.userExcluded") { value(false) }
                jsonPath("$.exclusionMessage") { doesNotExist() }
                jsonPath("$.userRestricted") { value(true) }
                jsonPath("$.restrictionMessage") { value(person.restrictionMessage) }
            }
    }

    @Test
    fun `case does not exist`() {
        val user = UserGenerator.TEST_USER1

        mockMvc.get("/user/${user.username}/access/NOTFOUND") { withToken() }
            .andExpect { status { isNotFound() } }
    }
}
