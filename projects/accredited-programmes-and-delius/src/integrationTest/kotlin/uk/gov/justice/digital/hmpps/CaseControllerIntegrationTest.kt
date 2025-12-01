// Kotlin
package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.TestData
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class CaseControllerIntegrationTest(
    @Autowired private val mockMvc: MockMvc
) {

    @Test
    fun `personal details 404`() {
        mockMvc.get("/case/DOESNOTEXIST/personal-details") {
            withToken()
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `personal details success`() {
        mockMvc.get("/case/${TestData.PERSON.crn}/personal-details") {
            withToken()
        }.andExpect {
            status { isOk() }
            content {
                json(
                    """
                {
                  "crn": "${TestData.PERSON.crn}",
                  "name": {
                    "forename": "Forename",
                    "middleNames": "MiddleName",
                    "surname": "Surname"
                  },
                  "dateOfBirth": "${LocalDate.now().minusYears(45).minusMonths(6)}",
                  "age": "45 years, 6 months",
                  "sex": {
                    "code": "M",
                    "description": "Male"
                  },
                  "ethnicity": {
                    "code": "A9",
                    "description": "Asian or Asian British: Other"
                  },
                  "probationPractitioner": {
                    "name": {
                      "forename": "Forename",
                      "surname": "Surname"
                    },
                    "code": "STAFF01",
                    "email": "test@example.com"
                  },
                  "team": {
                    "code": "TEAM01",
                    "description": "Test Team"
                  },
                  "probationDeliveryUnit": {
                    "code": "PDU1",
                    "description": "Test PDU"
                  },
                  "region": {
                    "code": "PA1",
                    "description": "Test Provider"
                  }
                }
                """.trimIndent(),
                    JsonCompareMode.STRICT
                )
            }
        }
    }

    @Test
    fun `sentence not found`() {
        mockMvc.get("/case/DOESNOTEXIST/sentence/1") {
            withToken()
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `custodial sentence success`() {
        mockMvc.get("/case/${TestData.PERSON.crn}/sentence/1") {
            withToken()
        }.andExpect {
            status { isOk() }
            content {
                json(
                    """
                    {
                      "description": "ORA Adult Custody (inc PSS) (24 Months)",
                      "startDate": "2000-01-01",
                      "expectedEndDate": "2100-01-01",
                      "licenceExpiryDate": "2050-01-01",
                      "postSentenceSupervisionEndDate": "2100-01-01",
                      "twoThirdsSupervisionDate": "2067-01-01",
                      "custodial": true,
                      "releaseType": "Released on Adult Licence",
                      "licenceConditions": [
                        {
                          "code": "NLC8",
                          "description": "Freedom of movement"
                        },
                        {
                          "code": "TEST",
                          "description": "To only attend specific places."
                        }
                      ],
                      "requirements": [],
                      "postSentenceSupervisionRequirements": [
                        {
                          "code": "S09",
                          "description": "Drug Testing"
                        },
                        {
                          "code": "TEST",
                          "description": "Pass drug tests"
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
    fun `un-sentenced event`() {
        mockMvc.get("/case/${TestData.PERSON.crn}/sentence/2") {
            withToken()
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `community sentence success`() {
        mockMvc.get("/case/${TestData.PERSON.crn}/sentence/3") {
            withToken()
        }.andExpect {
            status { isOk() }
            content {
                json(
                    """
                    {
                      "description": "ORA Community Order (6 Months)",
                      "startDate": "2000-01-01",
                      "expectedEndDate": "2100-01-01",
                      "custodial": false,
                      "licenceConditions": [],
                      "requirements": [
                        {
                          "code": "H",
                          "description": "Alcohol Treatment"
                        },
                        {
                          "code": "ALCTRT",
                          "description": "Alcohol Treatment"
                        }
                      ],
                      "postSentenceSupervisionRequirements": []
                    }
                    """.trimIndent(),
                    JsonCompareMode.STRICT
                )
            }
        }
    }

    @Test
    fun `offences not found`() {
        mockMvc.get("/case/DOESNOTEXIST/sentence/1/offences") {
            withToken()
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `offences success`() {
        mockMvc.get("/case/${TestData.PERSON.crn}/sentence/1/offences") {
            withToken()
        }.andExpect {
            status { isOk() }
            content {
                json(
                    """
                    {
                      "mainOffence": {
                        "date": "2000-01-01",
                        "mainCategoryCode": "036",
                        "mainCategoryDescription": "Kidnapping",
                        "subCategoryCode": "02",
                        "subCategoryDescription": "Hijacking"
                      },
                      "additionalOffences": [
                        {
                          "date": "2000-01-01",
                          "mainCategoryCode": "036",
                          "mainCategoryDescription": "Kidnapping",
                          "subCategoryCode": "03",
                          "subCategoryDescription": "False Imprisonment"
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
    fun `registrations success`() {
        mockMvc.get("/case/${TestData.PERSON.crn}/registrations") {
            withToken()
        }.andExpect {
            status { isOk() }
            content {
                json(
                    """
                    {
                      "registrations": [
                        {
                          "type": {
                            "code": "RVHR",
                            "description": "Very High RoSH"
                          },
                          "category": {
                            "code": "I3",
                            "description": "IOM - Fixed"
                          },
                          "date": "2000-01-01",
                          "nextReviewDate": "2000-06-01"
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
    fun `requirement success`() {
        mockMvc.get("/case/${TestData.PERSON.crn}/requirement/${TestData.REQUIREMENTS[0].id}") {
            withToken()
        }.andExpect {
            status { isOk() }
            content {
                json(
                    """
                    {
                      "manager": {
                        "staff": {
                          "name": {
                            "forename": "Forename",
                            "surname": "Surname"
                          },
                          "code": "STAFF01",
                          "email": "test@example.com"
                        },
                        "team": {
                          "code": "TEAM01",
                          "description": "Test Team"
                        },
                        "probationDeliveryUnit": {
                          "code": "PDU1",
                          "description": "Test PDU"
                        },
                        "officeLocations": [
                          {
                            "code": "OFFICE1",
                            "description": "Test Office Location"
                          }
                        ]
                      },
                      "probationDeliveryUnits": [
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
                      ]
                    }
                    """.trimIndent(),
                    JsonCompareMode.STRICT
                )
            }
        }
    }

    @Test
    fun `licence condition success`() {
        mockMvc.get("/case/${TestData.PERSON.crn}/licence-conditions/${TestData.LICENCE_CONDITIONS.first().id}") {
            withToken()
        }.andExpect {
            status { isOk() }
            content {
                json(
                    """
                    {
                      "manager": {
                        "staff": {
                          "name": {
                            "forename": "Forename",
                            "surname": "Surname"
                          },
                          "code": "STAFF01",
                          "email": "test@example.com"
                        },
                        "team": {
                          "code": "TEAM01",
                          "description": "Test Team"
                        },
                        "probationDeliveryUnit": {
                          "code": "PDU1",
                          "description": "Test PDU"
                        },
                        "officeLocations": [
                          {
                            "code": "OFFICE1",
                            "description": "Test Office Location"
                          }
                        ]
                      },
                      "probationDeliveryUnits": [
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
                      ]
                    }
                    """.trimIndent(),
                    JsonCompareMode.STRICT
                )
            }
        }
    }
}
