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
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class UserControllerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `returns teams for a user with valid username`() {
        val username = UserGenerator.DEFAULT_USER.distinguishedName!!
        mockMvc.get("/user/{username}/teams", username) { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "teams": [
                            {
                              "code": "T001",
                              "description": "Default Team",
                              "pdu": {
                                "code": "PDU001",
                                "description": "Central Borough"
                              },
                              "region": {
                                "code": "N01",
                                "description": "N01 Provider"
                              }
                            },
                            {
                              "code": "T002",
                              "description": "Second Team",
                              "pdu": {
                                "code": "PDU001",
                                "description": "Central Borough"
                              },
                              "region": {
                                "code": "N01",
                                "description": "N01 Provider"
                              }
                            }
                          ]
                        }
                        """.trimIndent(),
                        JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `matches usernames case insensitively`() {
        val username = UserGenerator.LONDON_USER.distinguishedName!!.lowercase()
        mockMvc.get("/user/{username}/teams", username) { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "teams": [
                            {
                              "code": "T_LON",
                              "description": "London Team",
                              "pdu": {
                                "code": "LPDU001",
                                "description": "Central London"
                              },
                              "region": {
                                "code": "N02",
                                "description": "N02 London Provider"
                              }
                            }
                          ]
                        }
                        """.trimIndent(),
                        JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `returns multiple teams across different regions for user`() {
        val username = UserGenerator.MULTI_TEAM_USER.distinguishedName!!
        mockMvc.get("/user/{username}/teams", username) { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "teams": [
                            {
                              "code": "T001",
                              "description": "Default Team",
                              "pdu": {
                                "code": "PDU001",
                                "description": "Central Borough"
                              },
                              "region": {
                                "code": "N01",
                                "description": "N01 Provider"
                              }
                            },
                            {
                              "code": "T003",
                              "description": "Third Team",
                              "pdu": {
                                "code": "PDU002",
                                "description": "East Borough"
                              },
                              "region": {
                                "code": "N01",
                                "description": "N01 Provider"
                              }
                            },
                            {
                              "code": "T_LON",
                              "description": "London Team",
                              "pdu": {
                                "code": "LPDU001",
                                "description": "Central London"
                              },
                              "region": {
                                "code": "N02",
                                "description": "N02 London Provider"
                              }
                            }
                          ]
                        }
                        """.trimIndent(),
                        JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `returns empty teams list for non-existent user`() {
        mockMvc.get("/user/{username}/teams", "CN=nonexistent.user,OU=staff,DC=justice,DC=gov,DC=uk") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json("""{ "teams": [] }""", JsonCompareMode.STRICT)
                }
            }
    }

    @Test
    fun `teams endpoint requires a valid bearer token`() {
        mockMvc.get("/user/{username}/teams", UserGenerator.DEFAULT_USER.distinguishedName!!)
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `returns teams sorted by provider code, then pdu, then lau, then team`() {
        val username = UserGenerator.MULTI_TEAM_USER.distinguishedName!!
        mockMvc.get("/user/{username}/teams", username) { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    jsonPath("$.teams[0].code") { value("T001") }
                    jsonPath("$.teams[1].code") { value("T003") }
                    jsonPath("$.teams[2].code") { value("T_LON") }
                }
            }
    }

    @Test
    fun `team hierarchy contains all required fields`() {
        val username = UserGenerator.DEFAULT_USER.distinguishedName!!
        mockMvc.get("/user/{username}/teams", username) { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    jsonPath("$.teams[0].code") { value("T001") }
                    jsonPath("$.teams[0].description") { value("Default Team") }
                    jsonPath("$.teams[0].pdu.code") { value("PDU001") }
                    jsonPath("$.teams[0].pdu.description") { value("Central Borough") }
                    jsonPath("$.teams[0].region.code") { value("N01") }
                    jsonPath("$.teams[0].region.description") { value("N01 Provider") }
                }
            }
    }
}

