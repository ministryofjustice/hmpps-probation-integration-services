package uk.gov.justice.digital.hmpps.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class RegionControllerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `returns all active regions and excludes inactive ones`() {
        mockMvc.get("/regions") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        [
                          { "code": "N01", "description": "N01 Provider" },
                          { "code": "N02", "description": "N02 London Provider" },
                          { "code": "N03", "description": "N03 Northern Provider" }
                        ]
                        """.trimIndent(),
                        JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `does not include the inactive provider in the regions list`() {
        mockMvc.get("/regions") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    jsonPath("$[?(@.code == 'N50')]") { isEmpty() }
                }
            }
    }

    @Test
    fun `returns active pdus for a region and excludes inactive ones`() {
        val region = ProviderGenerator.DEFAULT
        mockMvc.get("/regions/${region.code}/pdu") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        [
                          { "code": "PDU001", "description": "Central Borough" },
                          { "code": "PDU002", "description": "East Borough" },
                          { "code": "PDU003", "description": "North Borough" }
                        ]
                        """.trimIndent(),
                        JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `returns empty list of pdus for a region with no active pdus`() {
        val region = ProviderGenerator.NORTH
        mockMvc.get("/regions/${region.code}/pdu") { withToken() }
            .andExpect {
                status { isOk() }
                content { json("[]", JsonCompareMode.STRICT) }
            }
    }

    @Test
    fun `returns empty list of pdus for an unknown region code`() {
        mockMvc.get("/regions/UNKNOWN/pdu") { withToken() }
            .andExpect {
                status { isOk() }
                content { json("[]", JsonCompareMode.STRICT) }
            }
    }

    @Test
    fun `returns empty list of pdus when the region itself is inactive`() {
        val region = ProviderGenerator.INACTIVE
        mockMvc.get("/regions/${region.code}/pdu") { withToken() }
            .andExpect {
                status { isOk() }
                content { json("[]", JsonCompareMode.STRICT) }
            }
    }

    @Test
    fun `regions endpoint requires a valid bearer token`() {
        mockMvc.get("/regions")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `pdu endpoint requires a valid bearer token`() {
        mockMvc.get("/regions/${ProviderGenerator.DEFAULT.code}/pdu")
            .andExpect { status { isUnauthorized() } }
    }
}

