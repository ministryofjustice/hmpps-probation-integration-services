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
internal class AppointmentIntegrationTest @Autowired constructor(private val mockMvc: MockMvc) {
    @Test
    fun `unknown crn returns not found`() {
        mockMvc.get("/person/DOESNOTEXIST/future-appointments") { withToken() }
            .andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Person with CRN of DOESNOTEXIST not found") }
            }
    }

    @Test
    fun `get future appointments`() {
        mockMvc.get("/person/${PersonData.DEFAULT.crn}/future-appointments") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "content": [
                            {
                              "date": "2050-01-01",
                              "startTime": "09:00:00",
                              "endTime": "09:30:00",
                              "type": "Office Appointment",
                              "description": "Future appointment",
                              "practitioner": {
                                "name": {
                                  "forename": "Test",
                                  "surname": "Staff"
                                }
                              },
                              "location": {
                                "houseNumber": "123",
                                "buildingName": "Building name",
                                "street": "High Street",
                                "town": "Test Town",
                                "district": "Test District",
                                "county": "Test County",
                                "postcode": "TE1 1ST"
                              }
                            },
                            {
                              "date": "2050-01-01",
                              "startTime": "12:00:00",
                              "endTime": "13:30:00",
                              "type": "Office Appointment",
                              "description": "Future appointment - no location",
                              "practitioner": {
                                "name": {
                                  "forename": "Test",
                                  "surname": "Staff"
                                }
                              }
                            }
                          ],
                          "page": {
                            "size": 10,
                            "number": 0,
                            "totalElements": 2,
                            "totalPages": 1
                          }
                        }
                        """.trimIndent(),
                        JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `get past appointments`() {
        mockMvc.get("/person/${PersonData.DEFAULT.crn}/past-appointments") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "content": [
                            {
                              "date": "2020-01-01",
                              "startTime": "15:00:00",
                              "endTime": "15:30:00",
                              "type": "Office Appointment",
                              "description": "Past appointment - attended",
                              "practitioner": {
                                "name": {
                                  "forename": "Test",
                                  "surname": "Staff"
                                }
                              },
                              "location": {
                                "houseNumber": "123",
                                "buildingName": "Building name",
                                "street": "High Street",
                                "town": "Test Town",
                                "district": "Test District",
                                "county": "Test County",
                                "postcode": "TE1 1ST"
                              },
                              "attended": true,
                              "complied": true
                            },
                            {
                              "date": "2020-01-01",
                              "startTime": "10:00:00",
                              "endTime": "10:45:00",
                              "type": "Office Appointment",
                              "description": "Past appointment - not attended",
                              "practitioner": {
                                "name": {
                                  "forename": "Test",
                                  "surname": "Staff"
                                }
                              },
                              "location": {
                                "houseNumber": "123",
                                "buildingName": "Building name",
                                "street": "High Street",
                                "town": "Test Town",
                                "district": "Test District",
                                "county": "Test County",
                                "postcode": "TE1 1ST"
                              },
                              "attended": false,
                              "complied": false
                            }
                          ],
                          "page": {
                            "size": 10,
                            "number": 0,
                            "totalElements": 2,
                            "totalPages": 1
                          }
                        }
                        """.trimIndent(),
                        JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `supports pagination`() {
        mockMvc.get("/person/${PersonData.DEFAULT.crn}/future-appointments?page=1&size=1") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "content": [
                            {
                              "date": "2050-01-01",
                              "startTime": "12:00:00",
                              "endTime": "13:30:00",
                              "type": "Office Appointment",
                              "description": "Future appointment - no location",
                              "practitioner": {
                                "name": {
                                  "forename": "Test",
                                  "surname": "Staff"
                                }
                              }
                            }
                          ],
                          "page": {
                            "size": 1,
                            "number": 1,
                            "totalElements": 2,
                            "totalPages": 2
                          }
                        }
                        """.trimIndent(),
                        JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `person with no appointments has empty list in response`() {
        mockMvc.get("/person/${PersonData.BASIC.crn}/future-appointments") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "content": [],
                          "page": {
                            "size": 10,
                            "number": 0,
                            "totalElements": 0,
                            "totalPages": 0
                          }
                        }
                        """.trimIndent(),
                        JsonCompareMode.STRICT
                    )
                }
            }
    }
}
