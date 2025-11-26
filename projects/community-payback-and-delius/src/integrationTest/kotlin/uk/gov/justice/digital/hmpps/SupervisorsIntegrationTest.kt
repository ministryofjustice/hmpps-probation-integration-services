package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@SpringBootTest
@AutoConfigureMockMvc
class SupervisorsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `supervisor by username not found`() {
        mockMvc.get("/supervisors?username=NOTFOUND") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>().run {
                assertThat(message).isEqualTo("User with username of NOTFOUND not found")
            }
    }

    @Test
    fun `supervisor by username with no staff code`() {
        mockMvc.get("/supervisors?username=${UserGenerator.AUDIT_USER.username}") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>().run {
                assertThat(message).isEqualTo("Staff code for user with username of ${UserGenerator.AUDIT_USER.username} not found")
            }
    }

    @Test
    fun `supervisor by username returned`() {
        mockMvc.get("/supervisors?username=${UserGenerator.DEFAULT_USER.username}") { withToken() }
            .andExpect { status { isOk() } }
            .andExpect {
                content { json("""{"code": "N01P001", "isUnpaidWorkTeamMember": true}""", JsonCompareMode.STRICT) }
            }
    }
}
