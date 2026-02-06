package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.TestData.PROVIDER
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class GetRegionWIthMembersIntegrationTest(
    @Autowired private val mockMvc: MockMvc
) {
    @Test
    fun `can find region members`() {
        mockMvc.get("/regions/${PROVIDER.code}/members") { withToken() }
            .andExpect { status { isOk() } }
            .andExpect {
                content {
                    json(
                        """
                {
                  "code": "PA1",
                  "description": "Test Provider",
                  "pdus": [
                    {
                      "code": "PDU1",
                      "description": "Test PDU",
                      "team": [
                        {
                          "code": "TEAM01",
                          "description": "Test Team",
                          "members": [
                            {
                              "code": "STAFF01",
                              "name": {
                                "forename": "Forename",
                                "surname": "Surname"
                              }
                            }
                          ]
                        }
                      ]
                    },
                    {
                      "code": "PDU2",
                      "description": "A Second PDU",
                      "team": [
                        {
                          "code": "TEAM02",
                          "description": "A Second Team",
                          "members": [
                            {
                              "code": "STAFF02",
                              "name": {
                                "forename": "Forename",
                                "surname": "Surname"
                              }
                            }
                          ]
                        },
                        {
                          "code": "TEAM03",
                          "description": "A Third Team",
                          "members": [
                            {
                              "code": "STAFF03",
                              "name": {
                                "forename": "Forename",
                                "surname": "Surname"
                              }
                            },
                            {
                              "code": "STAFF04",
                              "name": {
                                "forename": "Forename",
                                "surname": "Surname"
                              }
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()
                    )
                }
            }
    }

    @Test
    fun `office locations returned for pdu`() {
        mockMvc.get("/regions/pdu/PDU1/office-locations") { withToken() }
            .andExpect { status { isOk() } }
            .andExpect {
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
            """.trimIndent()
                    )
                }
            }
    }
}
