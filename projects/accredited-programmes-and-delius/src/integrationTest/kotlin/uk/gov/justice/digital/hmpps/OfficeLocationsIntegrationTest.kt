package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.TestData
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class OfficeLocationsIntegrationTest(@Autowired private val mockMvc: MockMvc) {
    @Test
    fun `get office locations`() {
        mockMvc.get("/regions/pdu/${TestData.PDU.code}/office-locations") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "code": "PDU1",
                          "description": "Test PDU",
                          "officeLocations": [
                            {
                              "code": "OFFICE1",
                              "description": "Test Office Location"
                            }
                          ]
                        }
                        """.trimIndent(), JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `pdu linked to inactive region returns not found`() {
        mockMvc.get("/regions/pdu/${TestData.PDU_IN_INACTIVE_PROVIDER.code}/office-locations") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `office locations 404 for pdu not found`() {
        mockMvc.get("/regions/pdu/notfound/office-locations") { withToken() }
            .andExpect { status { isNotFound() } }
    }
}
