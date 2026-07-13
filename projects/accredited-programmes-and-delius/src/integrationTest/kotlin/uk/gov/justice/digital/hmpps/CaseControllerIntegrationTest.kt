package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.hasSize
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
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class CaseControllerIntegrationTest(
    @Autowired private val mockMvc: MockMvc
) {

    @Test
    fun `personal details 404`() {
        mockMvc.get("/case/DOESNOTEXIST/personal-details") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `personal details success`() {
        mockMvc.get("/case/${TestData.PERSON.crn}/personal-details") { withToken() }
            .andExpect {
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
        mockMvc.get("/case/DOESNOTEXIST/sentence/1") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `custodial sentence success`() {
        mockMvc.get("/case/${TestData.PERSON.crn}/sentence/1") { withToken() }
            .andExpect {
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
                              "code": "LAP",
                              "description": "Licence - Accredited Programmes"
                            },
                            {
                              "code": "TEST",
                              "description": "To only attend specific places."
                            },
                            {
                              "code": "LAP",
                              "description": "Licence - Accredited Programmes"
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
        mockMvc.get("/case/${TestData.PERSON.crn}/sentence/2") { withToken() }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `community sentence success`() {
        mockMvc.get("/case/${TestData.PERSON.crn}/sentence/3") { withToken() }
            .andExpect {
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
                              "code": "7",
                              "description": "Court - Accredited Programmes"
                            },
                            {
                              "code": "TEST",
                              "description": "Building Choices"
                            },
                            {
                              "code": "7",
                              "description": "Court - Accredited Programmes"
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
        mockMvc.get("/case/DOESNOTEXIST/sentence/1/offences") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `offences success`() {
        mockMvc.get("/case/${TestData.PERSON.crn}/sentence/1/offences") { withToken() }
            .andExpect {
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
                            },
                            {
                              "mainCategoryCode": "036",
                              "mainCategoryDescription": "Kidnapping",
                              "subCategoryCode": "01",
                              "subCategoryDescription": "Kidnapping"
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
        mockMvc.get("/case/${TestData.PERSON.crn}/registrations") { withToken() }
            .andExpect {
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
        mockMvc.get("/case/${TestData.PERSON.crn}/requirement/${TestData.REQUIREMENTS[0].id}") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("id") { value(TestData.REQUIREMENTS[0].id.toInt()) }
                jsonPath("mainCategory.code") { value("7") }
                jsonPath("mainCategory.description") { value("Court - Accredited Programmes") }
                jsonPath("manager.staff.name.forename") { value("Forename") }
                jsonPath("manager.staff.name.surname") { value("Surname") }
                jsonPath("manager.staff.code") { value("STAFF01") }
                jsonPath("manager.staff.email") { value("test@example.com") }
                jsonPath("manager.team.code") { value("TEAM01") }
                jsonPath("manager.team.description") { value("Test Team") }
                jsonPath("manager.probationDeliveryUnit.code") { value("PDU1") }
                jsonPath("manager.probationDeliveryUnit.description") { value("Test PDU") }
                jsonPath("manager.officeLocations", hasSize<Any>(1))
                jsonPath("manager.officeLocations[0].code") { value("OFFICE1") }
                jsonPath("manager.officeLocations[0].description") { value("Test Office Location") }
                jsonPath("probationDeliveryUnits", hasSize<Any>(1))
                jsonPath("probationDeliveryUnits[0].code") { value("PDU1") }
                jsonPath("probationDeliveryUnits[0].description") { value("Test PDU") }
                jsonPath("probationDeliveryUnits[0].officeLocations", hasSize<Any>(1))
                jsonPath("probationDeliveryUnits[0].officeLocations[0].code") { value("OFFICE1") }
                jsonPath("probationDeliveryUnits[0].officeLocations[0].description") { value("Test Office Location") }
                jsonPath("eventNumber") { value("3") }
            }
    }

    @Test
    fun `requirement not found`() {
        mockMvc.get("/case/${TestData.PERSON.crn}/requirement/999999") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `requirement crn not found`() {
        mockMvc.get("/case/DOESNOTEXIST/requirement/${TestData.REQUIREMENTS[0].id}") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `requirement belongs to different person returns 404`() {
        // REQUIREMENTS[2] belongs to CA_PERSON, not PERSON
        mockMvc.get("/case/${TestData.PERSON.crn}/requirement/${TestData.REQUIREMENTS[2].id}") { withToken() }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `requirements success`() {
        mockMvc
            .get("/case/${TestData.PERSON.crn}/requirements") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("content", hasSize<Any>(3))
                jsonPath(
                    "content[*].id",
                    hasItems(
                        TestData.REQUIREMENTS[0].id.toInt(),
                        TestData.REQUIREMENTS[1].id.toInt(),
                        TestData.REQUIREMENTS[3].id.toInt(),
                    )
                )
            }
    }

    @Test
    fun `requirements crn not found`() {
        mockMvc.get("/case/DOESNOTEXIST/requirements") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `licence condition success`() {
        mockMvc.get("/case/${TestData.PERSON.crn}/licence-conditions/${TestData.LICENCE_CONDITIONS.first().id}") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("id") { value(TestData.LICENCE_CONDITIONS.first().id.toInt()) }
                jsonPath("mainCategory.code") { value("LAP") }
                jsonPath("mainCategory.description") { value("Licence - Accredited Programmes") }
                jsonPath("manager.staff.name.forename") { value("Forename") }
                jsonPath("manager.staff.name.surname") { value("Surname") }
                jsonPath("manager.staff.code") { value("STAFF01") }
                jsonPath("manager.staff.email") { value("test@example.com") }
                jsonPath("manager.team.code") { value("TEAM01") }
                jsonPath("manager.team.description") { value("Test Team") }
                jsonPath("manager.probationDeliveryUnit.code") { value("PDU1") }
                jsonPath("manager.probationDeliveryUnit.description") { value("Test PDU") }
                jsonPath("manager.officeLocations", hasSize<Any>(1))
                jsonPath("manager.officeLocations[0].code") { value("OFFICE1") }
                jsonPath("manager.officeLocations[0].description") { value("Test Office Location") }
                jsonPath("probationDeliveryUnits", hasSize<Any>(1))
                jsonPath("probationDeliveryUnits[0].code") { value("PDU1") }
                jsonPath("probationDeliveryUnits[0].description") { value("Test PDU") }
                jsonPath("probationDeliveryUnits[0].officeLocations", hasSize<Any>(1))
                jsonPath("probationDeliveryUnits[0].officeLocations[0].code") { value("OFFICE1") }
                jsonPath("probationDeliveryUnits[0].officeLocations[0].description") { value("Test Office Location") }
                jsonPath("eventNumber") { value("1") }
            }
    }

    @Test
    fun `licence condition not found`() {
        mockMvc.get("/case/${TestData.PERSON.crn}/licence-conditions/999999") {
            withToken()
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `licence condition crn not found`() {
        mockMvc.get("/case/DOESNOTEXIST/licence-conditions/${TestData.LICENCE_CONDITIONS.first().id}") {
            withToken()
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `licence condition belongs to different person returns 404`() {
        // TERMINATION_LICENCE_CONDITION belongs to TERMINATION_PERSON, not PERSON
        mockMvc.get("/case/${TestData.PERSON.crn}/licence-conditions/${TestData.TERMINATION_LICENCE_CONDITION.id}") {
            withToken()
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `licence conditions success`() {
        mockMvc.get("/case/${TestData.PERSON.crn}/licence-conditions") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("content", hasSize<Any>(3))
                jsonPath(
                    "content[*].id",
                    hasItems(
                        TestData.LICENCE_CONDITIONS[0].id.toInt(),
                        TestData.LICENCE_CONDITIONS[1].id.toInt(),
                        TestData.LICENCE_CONDITIONS[2].id.toInt(),
                    )
                )
            }
    }

    @Test
    fun `licence conditions crn not found`() {
        mockMvc.get("/case/DOESNOTEXIST/licence-conditions") { withToken() }
            .andExpect { status { isNotFound() } }
    }
}
