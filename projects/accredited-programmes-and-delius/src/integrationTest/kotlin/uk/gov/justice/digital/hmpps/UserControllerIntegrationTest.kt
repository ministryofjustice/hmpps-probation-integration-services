package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.TestData
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class UserControllerIntegrationTest(@Autowired private val mockMvc: MockMvc) {

    @Test
    fun `access is allowed`() {
        mockMvc.post("/user/${TestData.USER.username}/access") {
            withToken()
            json = listOf(TestData.PERSON.crn)
        }
            .andExpect {
                status().isOk
                content {
                    json(
                        """
                    {
                      "access": [
                        {
                          "crn": "${TestData.PERSON.crn}",
                          "userExcluded": false,
                          "userRestricted": false
                        }
                      ]
                    }
                    """.trimIndent(), JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `access is limited`() {
        mockMvc.post("/user/${TestData.USER_WITH_LIMITED_ACCESS.username}/access") {
            withToken()
            json = listOf(TestData.PERSON.crn)
        }.andExpect {
            status { isOk() }
            content {
                json(
                    """
                    {
                      "access": [
                        {
                          "crn": "${TestData.PERSON.crn}",
                          "userExcluded": true,
                          "userRestricted": true,
                          "exclusionMessage":"Exclusion message",
                          "restrictionMessage":"Restriction message"
                        }
                      ]
                    }
                    """.trimIndent(), JsonCompareMode.STRICT
                )
            }
        }
    }

    @Test
    fun `get user team`() {
        mockMvc.get("/user/${TestData.USER.username}/teams") {
            withToken()
        }.andExpect {
            status { isOk() }
            content {
                json(
                    """
                    { "teams": [{"code":"TEAM01","description":"Test Team","pdu":{"code":"PDU1","description":"Test PDU"},"region":{"code":"PA1","description":"Test Provider"}}] }
                    """.trimIndent(), JsonCompareMode.STRICT
                )
            }
        }
    }

    @Test
    fun `access returns bad request when crns list is empty`() {
        mockMvc.post("/user/${TestData.USER.username}/access") {
            withToken()
            json = emptyList<String>()
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `access returns bad request when crns list exceeds 500 items`() {
        mockMvc.post("/user/${TestData.USER.username}/access") {
            withToken()
            json = (1..501).map { "CRN$it" }
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
