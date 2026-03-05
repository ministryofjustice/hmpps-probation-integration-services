package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.TestData.PersonData
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class PersonalDetailsIntegrationTest @Autowired constructor(private val mockMvc: MockMvc) {
    @Test
    fun `unknown crn returns not found`() {
        mockMvc.get("/person/DOESNOTEXIST/name") { withToken() }
            .andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Person with CRN of DOESNOTEXIST not found") }
            }
    }

    @Test
    fun `get person name`() {
        mockMvc.get("/person/${PersonData.DEFAULT.crn}/name") { withToken() }
            .andExpect {
                status { isOk() }
                content { json("""{"forename":"Test","middleName":"Middle Name","surname":"One"}""") }
            }
    }

    @Test
    fun `get personal details`() {
        mockMvc.get("/person/${PersonData.DEFAULT.crn}/personal-details") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "name": {
                            "forename": "Test",
                            "middleName": "Middle Name",
                            "surname": "One"
                          },
                          "preferredName": "Tester",
                          "dateOfBirth": "1990-01-01",
                          "mainAddress": {
                            "houseNumber": "1",
                            "buildingName": "My Building",
                            "street": "My Street",
                            "town": "My Town",
                            "district": "My District",
                            "county": "My County",
                            "postcode": "TE1 1ST"
                          },
                          "telephoneNumber": "01000000001",
                          "mobileNumber": "07111111111",
                          "emailAddress": "person.one@example.com",
                          "emergencyContacts": [
                            {
                              "name": {
                                "forename": "Joe",
                                "surname": "Bloggs"
                              },
                              "relationship": "Sister",
                              "mobileNumber": "07333333333",
                              "emailAddress": "joe.bloggs@example.com"
                            }
                          ],
                          "practitioner": {
                            "name": {
                              "forename": "Test",
                              "surname": "Staff"
                            },
                            "telephoneNumber": "07000000000",
                            "team": {
                              "officeAddresses": [
                                {
                                  "houseNumber": "123",
                                  "buildingName": "Building name",
                                  "street": "High Street",
                                  "town": "Test Town",
                                  "district": "Test District",
                                  "county": "Test County",
                                  "postcode": "TE1 1ST"
                                }
                              ]
                            }
                          }
                        }
                        """.trimIndent(),
                        JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `person with no main address has null main address in response`() {
        mockMvc.get("/person/${PersonData.BASIC.crn}/personal-details") { withToken() }.andExpect {
            status { isOk() }
            jsonPath("$.mainAddress") { doesNotExist() }
            jsonPath("$.emergencyContacts.length()") { value(0) }
            jsonPath("$.practitioner.telephoneNumber") { doesNotExist() }
        }
    }
}
