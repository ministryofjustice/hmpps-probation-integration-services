package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.TestData
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class UserControllerIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `access is allowed`() {
        mockMvc
            .perform(
                post("/user/${TestData.USER.username}/access")
                    .withToken()
                    .withJson(listOf(TestData.PERSON.crn))
            )
            .andExpect(status().isOk)
            .andExpect(
                content().json(
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
            )
    }

    @Test
    fun `access is limited`() {
        mockMvc
            .perform(
                post("/user/${TestData.USER_WITH_LIMITED_ACCESS.username}/access")
                    .withToken()
                    .withJson(listOf(TestData.PERSON.crn))
            )
            .andExpect(status().isOk)
            .andExpect(
                content().json(
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
            )
    }
}
