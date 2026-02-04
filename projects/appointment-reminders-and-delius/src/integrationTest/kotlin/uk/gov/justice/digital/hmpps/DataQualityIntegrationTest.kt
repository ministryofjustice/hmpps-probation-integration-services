package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class DataQualityIntegrationTest(
    @Autowired private val mockMvc: MockMvc
) {

    @Test
    fun `retrieves count of cases with an invalid mobile number`() {
        mockMvc
            .get("/data-quality/${ProviderGenerator.LONDON.code}/invalid-mobile-numbers/count") { withToken() }
            .andExpect {
                status { isOk() }
                content { string("2") }
            }
    }

    @Test
    fun `retrieves cases with an invalid mobile number`() {
        mockMvc
            .get("/data-quality/${ProviderGenerator.LONDON.code}/invalid-mobile-numbers") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                      {
                        "content": [
                          {
                            "name": "Test Person",
                            "crn": "A000004",
                            "mobileNumber": "07000000004 invalid",
                            "manager": {
                              "name": "Test Staff",
                              "email": "test@example.com"
                            },
                            "probationDeliveryUnit": "Croydon"
                          },
                          {
                            "name": "Test Person",
                            "crn": "A000005",
                            "mobileNumber": "070000005",
                            "manager": {
                              "name": "Test Staff",
                              "email": "test@example.com"
                            },
                            "probationDeliveryUnit": "Croydon"
                          }
                        ],
                        "page": {
                          "size": 10,
                          "number": 0,
                          "totalElements": 2,
                          "totalPages": 1
                        }
                      }
                      """.trimIndent(), JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `retrieves cases with a missing mobile number`() {
        mockMvc
            .get("/data-quality/${ProviderGenerator.LONDON.code}/missing-mobile-numbers") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "content": [
                            {
                              "name": "Test Person",
                              "crn": "A000006",
                              "manager": {
                                "name": "Test Staff",
                                "email": "test@example.com"
                              },
                              "probationDeliveryUnit": "Croydon"
                            }
                          ],
                          "page": {
                            "size": 10,
                            "number": 0,
                            "totalElements": 1,
                            "totalPages": 1
                          }
                        }
                        """.trimIndent(), JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `retrieves cases with duplicate mobile numbers`() {
        mockMvc
            .get("/data-quality/${ProviderGenerator.LONDON.code}/duplicate-mobile-numbers") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "content": [
                            {
                              "name": "Test Person",
                              "crn": "A000002",
                              "mobileNumber": "07000000002",
                              "manager": {
                                "name": "Test Staff",
                                "email": "test@example.com"
                              },
                              "probationDeliveryUnit": "Croydon"
                            },
                            {
                              "name": "Test Person",
                              "crn": "A000003",
                              "mobileNumber": "07000000002",
                              "manager": {
                                "name": "Test Staff",
                                "email": "test@example.com"
                              },
                              "probationDeliveryUnit": "Croydon"
                            }
                          ],
                          "page": {
                            "size": 10,
                            "number": 0,
                            "totalElements": 2,
                            "totalPages": 1
                          }
                        }
                        """.trimIndent(), JsonCompareMode.STRICT
                    )
                }
            }
    }
}
