package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class CorePersonIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `correctly returns detail by crn`() {
        mockMvc
            .perform(get("/probation-cases/${PersonGenerator.MIN_PERSON.crn}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(
                content().json(
                    """
                    {
                      "identifiers": {
                        "deliusId": ${PersonGenerator.MIN_PERSON.id},
                        "crn": "M123456",
                        "additionalIdentifiers": []
                      },
                      "name": {
                        "forename": "Isabelle",
                        "surname": "Necessary"
                      },
                      "dateOfBirth": "1990-03-05",
                      "aliases": [],
                      "addresses": [],
                      "sentences": [],
                      "religionHistory": []
                    }
                    """.trimIndent(),
                    JsonCompareMode.STRICT,
                )
            )
    }

    @Test
    fun `correctly returns detail by id`() {
        mockMvc
            .perform(get("/probation-cases/${PersonGenerator.FULL_PERSON.id}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(
                content().json(
                    """
                    {
                      "identifiers": {
                        "deliusId": ${PersonGenerator.FULL_PERSON.id},
                        "crn": "F123456",
                        "nomsId": "A3349EX",
                        "prisonerNumber": "94600E",
                        "pnc": "2011/0593710D",
                        "cro": "89861/11W",
                        "ni": "FJ123456W",
                        "additionalIdentifiers": [
                          {
                            "type": {
                              "code": "DRL",
                              "description": "Drivers Licence"
                            },
                            "value": "BANTE707155F99XX"
                          }
                        ]
                      },
                      "name": {
                        "forename": "Frederick",
                        "middleName": "Paul Bernard",
                        "surname": "Johnson",
                        "previousSurname": "No Previous",
                        "preferred": "Freddy"
                      },
                      "dateOfBirth": "1975-07-15",
                      "dateOfDeath": "2015-08-15", 
                      "title": {
                        "code": "TIT",
                        "description": "Description of TIT"
                      },
                      "gender": {
                        "code": "GEN",
                        "description": "Description of GEN"
                      },
                      "genderIdentity": {
                        "code": "GID",
                        "description": "Description of GID"
                      },
                      "genderIdentityDescription": "Self-described gender identity",
                      "nationality": {
                        "code": "NAT",
                        "description": "Description of NAT"
                      },
                      "secondNationality": {
                        "code": "NAT",
                        "description": "Description of NAT"
                      },
                      "ethnicity": {
                        "code": "ETH",
                        "description": "Description of ETH"
                      },
                      "ethnicityDescription": "Self-described ethnicity",
                      "religion": {
                        "code": "REL",
                        "description": "Description of REL"
                      },
                      "religionHistory": [
                        {
                          "code": "REL_HX",
                          "description": "Description of REL_HX",
                          "startDate": "${LocalDate.now().minusDays(30)}",
                          "endDate": "${LocalDate.now().minusDays(10)}",
                          "lastUpdatedBy": "User1",
                          "lastUpdatedAt": "${PersonGenerator.UPDATED_ZONED_DATETIME.toLocalDateTime()}Z"
                          
                        },
                        {
                          "description": "Self-described religion",
                          "startDate": "${LocalDate.now().minusDays(10)}",
                          "endDate": "${LocalDate.now().minusDays(1)}",
                          "lastUpdatedBy": "User1",
                          "lastUpdatedAt": "${PersonGenerator.UPDATED_ZONED_DATETIME.toLocalDateTime()}Z"
                        }
                      ],
                      "religionDescription": "Self-described faith",
                      "sexualOrientation": {
                        "code": "SEO",
                        "description": "Description of SEO"
                      },
                      "contactDetails": {
                        "telephone": "0191 755 4789",
                        "mobile": "07895746789",
                        "email": "fred@gmail.com"
                      },
                      "aliases": [
                        {
                          "name": {
                            "forename": "Freddy",
                            "surname": "Banter"
                          },
                          "dateOfBirth": "1974-02-17",
                          "gender": {
                            "code": "GEN",
                            "description": "Description of GEN"
                          }
                        }
                      ],
                      "addresses": [
                        {
                          "fullAddress": "1 Main Street, London, PC1 1TS",
                          "addressNumber": "1",
                          "streetName": "Main Street",
                          "district": "London",
                          "county": "   ",
                          "postcode": "PC1 1TS",
                          "uprn": 123456789,
                          "telephoneNumber": "01234 567890",
                          "noFixedAbode": false,
                          "status": {
                            "code": "M",
                            "description": "Main Address"
                          },
                          "notes": "Some notes about this address",
                          "startDate": "${LocalDate.now().minusDays(30)}"
                        },
                        {
                          "fullAddress": "NF1 1NF",
                          "postcode": "NF1 1NF",
                          "noFixedAbode": true,
                          "status": {
                            "code": "P",
                            "description": "Previous Address"
                          },
                          "startDate": "${LocalDate.now().minusDays(60)}",
                          "endDate": "${LocalDate.now().minusDays(30)}"
                        }
                      ],
                      "excludedFrom": {
                        "message": "This case is excluded because ...",
                        "users": [{ "username": "SomeUser1" }]
                      },
                      "restrictedTo": {
                        "message": "This case is restricted because ...",
                        "users": [
                          { "username": "SomeUser2" },
                          { "username": "FutureEndDatedUser" }
                        ]
                      },
                      "sentences": [
                        {
                          "date": "2024-08-07",
                          "active": true
                        },
                        {
                          "date": "2024-08-05",
                          "active": false
                        },
                        {
                          "date": "2024-08-03",
                          "active": true
                        }
                      ]
                    }
                    """.trimIndent(),
                    JsonCompareMode.STRICT,
                )
            )
    }

    @Test
    fun `correctly returns all cases`() {
        mockMvc
            .perform(get("/all-probation-cases?sort=crn,desc").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("page.totalElements", equalTo(2)))
            .andExpect(jsonPath("content[0].identifiers.crn", equalTo(PersonGenerator.MIN_PERSON.crn)))
            .andExpect(jsonPath("content[1].identifiers.crn", equalTo(PersonGenerator.FULL_PERSON.crn)))
    }
}
