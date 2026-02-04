package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.data.generator.DetailsGenerator
import uk.gov.justice.digital.hmpps.data.generator.SearchGenerator
import uk.gov.justice.digital.hmpps.data.generator.SearchGenerator.JOHN_SMITH_1
import uk.gov.justice.digital.hmpps.data.generator.SearchGenerator.JOHN_SMITH_1_ALIAS
import uk.gov.justice.digital.hmpps.data.generator.SearchGenerator.JOHN_SMITH_2
import uk.gov.justice.digital.hmpps.entity.DetailPerson
import uk.gov.justice.digital.hmpps.entity.PersonAlias
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SearchIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `search must have at least one parameter`() {
        mockMvc.post("/search/probation-cases") {
            withToken()
            json = SearchRequest()
        }
            .andExpect { status { is4xxClientError() } }
    }

    @Test
    fun `can find matching all fields`() {
        val person = SearchGenerator.JOHN_DOE
        val request = SearchRequest(
            person.forename,
            person.surname,
            person.dateOfBirth,
            person.pncNumber,
            person.crn,
            person.nomsNumber
        )
        val expectedJson = /* language=json */ """
        [
          {
            "firstName": "John",
            "middleNames": [],
            "surname": "Doe",
            "dateOfBirth": "1998-02-23",
            "gender": "MALE",
            "otherIds": {
              "crn": "S123456",
              "nomsNumber": "S3477CH",
              "pncNumber": "1964/8284523P"
            },
            "offenderProfile": {
              "nationality": "BRITISH",
              "religion": "Jedi"
            },
            "offenderAliases": [],
            "offenderManagers": [
              {
                "staff": {
                  "code": "LNDMCDS",
                  "forenames": "Simon",
                  "surname": "Smith",
                  "unallocated": false
                },
                "team": {
                  "code": "LNDMCD",
                  "description": "Description of LNCMCD",
                  "district": {
                    "code": "KK",
                    "description": "Kings Cross"
                  }
                },
                "probationArea": {
                  "code": "LDN",
                  "description": "London",
                  "localDeliveryUnits": []
                },
                "active": true
              }
            ]
          }
        ]
        """.trimIndent()

        mockMvc.post("/search/probation-cases") {
            withToken()
            json = request
        }
            .andExpect {
                status { is2xxSuccessful() }
                content {
                    json(expectedJson, JsonCompareMode.STRICT)
                }
            }

        mockMvc.post("/search/probation-cases?useSearch=false") {
            withToken()
            json = request
        }
            .andExpect {
                status { is2xxSuccessful() }
                content {
                    json(expectedJson, JsonCompareMode.STRICT)
                }
            }
    }

    @Test
    fun `can find all matching names`() {
        val request = SearchRequest("John", "Smith")
        val expectedJson = /* language=json */ """
        [
          {
            "firstName": "John",
            "middleNames": [],
            "surname": "Smith",
            "dateOfBirth": "1998-01-01",
            "gender": "MALE",
            "otherIds": {
              "crn": "S223456",
              "nomsNumber": "S3478CH"
            },
            "offenderProfile": {
              "nationality": "BRITISH",
              "religion": "Jedi"
            },
            "offenderAliases": [],
            "offenderManagers": [
              {
                "staff": {
                  "code": "LNDMCDS",
                  "forenames": "Simon",
                  "surname": "Smith",
                  "unallocated": false
                },
                "team": {
                  "code": "LNDMCD",
                  "description": "Description of LNCMCD",
                  "district": {
                    "code": "KK",
                    "description": "Kings Cross"
                  }
                },
                "probationArea": {
                  "code": "LDN",
                  "description": "London",
                  "localDeliveryUnits": []
                },
                "active": true
              }
            ],
            "mappa": {
              "level": 1,
              "levelDescription": "MAPPA Level 1",
              "category": 4,
              "categoryDescription": "MAPPA Category 4",
              "startDate": "2024-12-31",
              "reviewDate": "2026-12-31",
              "team": {
                "code": "LNDMCD",
                "description": "Description of LNCMCD"
              },
              "officer": {
                "code": "LNDMCDS",
                "forenames": "Simon",
                "surname": "Smith",
                "unallocated": false
              },
              "probationArea": {
                "code": "LDN",
                "description": "London"
              },
              "notes": "Some notes"
            }
          },
          {
            "firstName": "John",
            "middleNames": [],
            "surname": "Smith",
            "dateOfBirth": "1998-12-12",
            "gender": "MALE",
            "otherIds": {
              "crn": "S223457",
              "nomsNumber": "S3479CH"
            },
            "offenderProfile": {
              "nationality": "BRITISH",
              "religion": "Jedi"
            },
            "offenderAliases": [],
            "offenderManagers": [
              {
                "staff": {
                  "code": "LNDMCDS",
                  "forenames": "Simon",
                  "surname": "Smith",
                  "unallocated": false
                },
                "team": {
                  "code": "LNDMCD",
                  "description": "Description of LNCMCD",
                  "district": {
                    "code": "KK",
                    "description": "Kings Cross"
                  }
                },
                "probationArea": {
                  "code": "LDN",
                  "description": "London",
                  "localDeliveryUnits": []
                },
                "active": true
              }
            ]
          }
        ]
        """.trimIndent()

        mockMvc.post("/search/probation-cases") {
            withToken()
            json = request
        }
            .andExpect {
                status { is2xxSuccessful() }
                content {
                    json(expectedJson, JsonCompareMode.STRICT)
                }
            }

        mockMvc.post("/search/probation-cases?useSearch=false") {
            withToken()
            json = request
        }
            .andExpect {
                status { is2xxSuccessful() }
                content {
                    json(expectedJson, JsonCompareMode.STRICT)
                }
            }
    }

    @Test
    fun `can find all matching crn`() {
        val request = SearchRequest(crn = JOHN_SMITH_2.crn)

        mockMvc.post("/search/probation-cases") {
            withToken()
            json = request
        }
            .andExpect { status { is2xxSuccessful() } }
            .andExpectJson(listOf(JOHN_SMITH_2.asProbationCase()))

        mockMvc.post("/search/probation-cases?useSearch=false") {
            withToken()
            json = request
        }
            .andExpect { status { is2xxSuccessful() } }
            .andExpectJson(listOf(JOHN_SMITH_2.asProbationCase()))
    }

    @Test
    fun `can find all matching noms`() {
        val request = SearchRequest(nomsNumber = JOHN_SMITH_1.nomsNumber)

        mockMvc.post("/search/probation-cases") {
            withToken()
            json = request
        }
            .andExpect { status { is2xxSuccessful() } }
            .andExpectJson(listOf(JOHN_SMITH_1.asProbationCase()))

        mockMvc.post("/search/probation-cases?useSearch=false") {
            withToken()
            json = request
        }
            .andExpect { status { is2xxSuccessful() } }
            .andExpectJson(listOf(JOHN_SMITH_1.asProbationCase()))
    }

    @Test
    fun `must provide at least one crn for crn lookup`() {
        mockMvc.post("/search/probation-cases/crns") {
            withToken()
            json = listOf<String>()
        }
            .andExpect { status { is4xxClientError() } }
    }

    @Test
    fun `can find all by crns`() {
        mockMvc.post("/search/probation-cases/crns") {
            withToken()
            json = listOf(JOHN_SMITH_1.crn, JOHN_SMITH_2.crn)
        }
            .andExpect { status { is2xxSuccessful() } }
            .andExpectJson(
                listOf(
                    JOHN_SMITH_1.asProbationCase(JOHN_SMITH_1_ALIAS),
                    JOHN_SMITH_2.asProbationCase()
                )
            )
    }

    private fun DetailPerson.asProbationCase(alias: PersonAlias? = null): OffenderDetail {
        val staff = DetailsGenerator.STAFF
        val team = DetailsGenerator.TEAM
        val probationArea = DetailsGenerator.DEFAULT_PA
        return OffenderDetail(
            firstName = forename,
            surname = surname,
            dateOfBirth = dateOfBirth,
            gender = gender.description,
            otherIds = IDs(crn, nomsNumber, pncNumber),
            offenderProfile = OffenderProfile(
                ethnicity?.description,
                nationality?.description,
                religion?.description
            ),
            offenderManagers = listOf(
                OffenderManager(
                    staff = StaffHuman(staff.code, staff.forename, staff.surname, staff.unallocated()),
                    team = SearchResponseTeam(
                        team.code,
                        team.description,
                        KeyValue(team.district.code, team.district.description)
                    ),
                    probationArea = ProbationArea(probationArea.code, probationArea.description, listOf()),
                )
            ),
            offenderAliases = listOfNotNull(alias?.let {
                OffenderAlias(
                    id = it.aliasID,
                    dateOfBirth = it.dateOfBirth,
                    firstName = it.firstName,
                    middleNames = listOfNotNull(it.secondName, it.thirdName),
                    surname = it.surname,
                    gender = it.gender.description
                )
            })
        )
    }
}