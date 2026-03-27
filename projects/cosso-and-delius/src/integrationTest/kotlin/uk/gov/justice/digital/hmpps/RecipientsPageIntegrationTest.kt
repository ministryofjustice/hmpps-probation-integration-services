package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class RecipientsPageIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @Test
    fun `can retrieve SRF email domains`() {
        val expectedResponse =
            """{"authorisedEmails":[{"code":"J","description":"justice.gov.uk"},{"code":"P","description":"police.gov.uk"}]}"""
        mockMvc.get("/authorised-emails") { withToken() }
            .andExpect {
                status { isOk() }
                content { json(expectedResponse, JsonCompareMode.STRICT) }
            }
    }
}