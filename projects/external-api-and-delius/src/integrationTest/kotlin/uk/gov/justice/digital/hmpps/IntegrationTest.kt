package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.PERSON
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator.REFDATA_FEMALE
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator.REFDATA_MALE
import uk.gov.justice.digital.hmpps.model.PersonIdentifier
import uk.gov.justice.digital.hmpps.model.ProbationReferenceData
import uk.gov.justice.digital.hmpps.model.RefData
import uk.gov.justice.digital.hmpps.service.PhoneTypes
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `returns supervisions`() {
        val start = LocalDate.now()
        val review = LocalDate.now().plusMonths(6)
        mockMvc
            .perform(get("/case/${PERSON.crn}/supervisions").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(
                content().json(
                    """
                    {
                        "communityManager": {
                          "code": "DEFJOSM",                 
                          "name": {
                            "forename": "John",
                            "surname": "Smith"
                          },   
                          "username": "john-smith",
                          "email": "john.smith@moj.gov.uk",
                          "telephoneNumber": "07321165373",
                          "team": {
                            "code": "DEFUAT",
                            "description": "Default Team",
                            "email": "team@justice.co.uk",
                            "telephoneNumber": "020 334 1257",
                            "provider": {
                              "code": "DEF",
                              "description": "Default Provider"
                            }
                          }              
                        },
                        "mappaDetail": {
                          "level": 1,                 
                          "levelDescription": "Description of M1",   
                          "category": 2,              
                          "categoryDescription": "Description of M2",
                          "startDate": "$start",        
                          "reviewDate": "$review",      
                          "notes": "Mappa Detail for ${PERSON.crn}"               
                        },
                        "supervisions": [
                            {
                                "number": 1,
                                "active": true,
                                "date": "2023-01-02",
                                "sentence": {
                                    "description": "ORA Suspended Sentence Order",
                                    "date": "2023-03-04",
                                    "length": 6,
                                    "lengthUnits": "Months",
                                    "custodial": true
                                },
                                "mainOffence": {
                                    "date": "2023-01-01",
                                    "count": 1,
                                    "code": "12345",
                                    "description": "Test offence",
                                    "mainCategory": {
                                        "code": "123",
                                        "description": "Test"
                                    },
                                    "subCategory": {
                                        "code": "45",
                                        "description": "offence"
                                    },
                                    "schedule15SexualOffence": true
                                },
                                "additionalOffences": [
                                    {
                                        "count": 3,
                                        "code": "12345",
                                        "description": "Test offence",
                                        "mainCategory": {
                                            "code": "123",
                                            "description": "Test"
                                        },
                                        "subCategory": {
                                            "code": "45",
                                            "description": "offence"
                                        },
                                        "schedule15SexualOffence": true
                                    }
                                ],
                                "courtAppearances": [
                                    {
                                        "type": "Sentence",
                                        "date": "2023-02-03T10:00:00Z",
                                        "court": "Manchester Crown Court",
                                        "plea": "Not guilty"
                                    }
                                ]
                            }
                        ],
                        "dynamicRisks": [
                            {"code": "RCCO", "description": "Description for RCCO", "startDate": "$start"},
                            {"code": "RCPR", "description": "Description for RCPR", "startDate": "$start"}
                        ],
                        "personStatus": [{"code":"ASFO", "description": "Description for ASFO", "startDate": "$start"}]
                    }
                    """.trimIndent()
                )
            )
    }

    @Test
    fun `returns crn for nomsId`() {

        val detailResponse = mockMvc
            .perform(get("/identifier-converter/noms-to-crn/${PERSON.nomsId}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<PersonIdentifier>()
        Assertions.assertEquals(detailResponse.crn, PERSON.crn)
    }

    @Test
    fun `returns 404 for nomsId not found`() {
        mockMvc.perform(get("/identifier-converter/noms-to-crn/A0001DZ").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `returns reference data for ethnicity, gender and register_types`() {
        val response = mockMvc.perform(get("/reference-data").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<ProbationReferenceData>()
        Assertions.assertEquals(
            response.probationReferenceData["GENDER"],
            listOf(
                RefData(REFDATA_FEMALE.code, REFDATA_FEMALE.description),
                RefData(REFDATA_MALE.code, REFDATA_MALE.description)
            )
        )
        Assertions.assertEquals(
            response.probationReferenceData["PHONE_TYPE"],
            listOf(
                RefData(PhoneTypes.TELEPHONE.name, PhoneTypes.TELEPHONE.description),
                RefData(PhoneTypes.MOBILE.name, PhoneTypes.MOBILE.description)
            )
        )
    }
}
