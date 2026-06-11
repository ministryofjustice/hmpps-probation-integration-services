package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT_PERSON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PERSON_IN_PRISON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PERSON_WITH_RESPONSIBLE_OFFICER_WITHOUT_USER
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ResponsibleOfficerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @Test
    fun `can get responsible officer details for probation om`() {
        val crn = DEFAULT_PERSON.crn
        mockMvc.get("/responsible-officer/$crn") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "name": {
                            "forename": "John",
                            "middleName": "Bob",
                            "surname": "Smith"
                          },
                          "emailAddress": "john.smith@moj.gov.uk",
                          "telephoneNumber": "07247764536",
                          "probationArea": {
                            "code": "N02",
                            "description": "DEFAULT_PROBATION_AREA"
                          },
                          "replyAddresses": [
                            {
                              "id": 10001099,
                              "status": "Default",
                              "officeDescription": "Main Office",
                              "buildingName": "The Office Block",
                              "buildingNumber": "1",
                              "streetName": "The Street",
                              "townCity": "The Town",
                              "district": "The District",
                              "county": "The County",
                              "postcode": "AA1 1AA"
                            },
                            {
                              "id": 10001100,
                              "officeDescription": "Office 2",
                              "buildingName": "Office Block 2",
                              "buildingNumber": "2",
                              "streetName": "Street 2",
                              "townCity": "Town 2",
                              "district": "District 2",
                              "county": "County 2",
                              "postcode": "AA1 1AB"
                            }
                          ]
                        }
                        """, JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `can get responsible officer details for prison om`() {
        val crn = PERSON_IN_PRISON.crn

        mockMvc.get("/responsible-officer/$crn") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "name": {
                            "forename": "John",
                            "middleName": "Bob",
                            "surname": "Smith"
                          },
                          "emailAddress": "john.smith@moj.gov.uk",
                          "telephoneNumber": "07247764536",
                          "probationArea": {
                            "code": "N02",
                            "description": "DEFAULT_PROBATION_AREA"
                          },
                          "replyAddresses": [
                            {
                              "id": 10001099,
                              "status": "Default",
                              "officeDescription": "Main Office",
                              "buildingName": "The Office Block",
                              "buildingNumber": "1",
                              "streetName": "The Street",
                              "townCity": "The Town",
                              "district": "The District",
                              "county": "The County",
                              "postcode": "AA1 1AA"
                            },
                            {
                              "id": 10001100,
                              "officeDescription": "Office 2",
                              "buildingName": "Office Block 2",
                              "buildingNumber": "2",
                              "streetName": "Street 2",
                              "townCity": "Town 2",
                              "district": "District 2",
                              "county": "County 2",
                              "postcode": "AA1 1AB"
                            }
                          ]
                        }
                        """, JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `can get responsible officer details when user is missing`() {
        val crn = PERSON_WITH_RESPONSIBLE_OFFICER_WITHOUT_USER.crn
        mockMvc.get("/responsible-officer/$crn") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "name": {
                            "forename": "Jane",
                            "middleName": "Mary",
                            "surname": "Doe"
                          },
                          "probationArea": {
                            "code": "N02",
                            "description": "DEFAULT_PROBATION_AREA"
                          },
                          "replyAddresses": []
                        }
                        """, JsonCompareMode.STRICT
                    )
                }
            }
    }
}
