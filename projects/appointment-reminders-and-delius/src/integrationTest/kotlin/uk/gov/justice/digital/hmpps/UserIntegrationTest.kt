package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class UserIntegrationTest(
    @Autowired private val mockMvc: MockMvc
) {

    @Test
    fun `retrieves user's providers`() {
        mockMvc.get("/users/${UserGenerator.TEST_USER.username}/providers") {
            withToken()
        }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "providers": [
                            {
                              "code": "N07",
                              "name": "London"
                            }
                          ]
                        }
                        """.trimIndent(), JsonCompareMode.STRICT
                    )
                }
            }
    }
}
