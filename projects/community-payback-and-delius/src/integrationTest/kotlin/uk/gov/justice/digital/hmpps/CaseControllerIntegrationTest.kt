package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.model.ScheduleResponse
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest
class CaseControllerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @Test
    fun `returns schedule with requirement progress, allocations and appointments`() {
        val response = mockMvc
            .get("/case/${PersonGenerator.DEFAULT_PERSON.crn}/event/${UPWGenerator.EVENT_1.number}/appointments/schedule") {
                withToken()
            }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<ScheduleResponse>()

        assertThat(response.requirementProgress).isNotNull
        assertThat(response.requirementProgress.requiredMinutes).isEqualTo(7200) // rqmnt length of 120 hours * 60
        assertThat(response.requirementProgress.completedMinutes).isEqualTo(870) // sum of minutesCredited on appointments

        assertThat(response.allocations).isNotEmpty
        val allocation = response.allocations.first()
        assertThat(allocation.id).isEqualTo(UPWGenerator.DEFAULT_UPW_ALLOCATION.id)
        assertThat(allocation.pickUp!!.time).isEqualTo(UPWGenerator.DEFAULT_UPW_ALLOCATION.pickUpTime)
        assertThat(allocation.pickUp!!.location!!.code).isEqualTo(UPWGenerator.DEFAULT_OFFICE_LOCATION.code)
        assertThat(allocation.pickUp!!.location!!.description).isEqualTo(UPWGenerator.DEFAULT_OFFICE_LOCATION.description)
        assertThat(allocation.project.name).isEqualTo("Default UPW Project")
        assertThat(allocation.project.code).isEqualTo("N01P01")
        assertThat(allocation.project.expectedEndDateExclusive).isEqualTo(UPWGenerator.UPW_PROJECT_1.expectedEndDate)
        assertThat(allocation.dayOfWeek).isEqualTo("MONDAY")
        assertThat(allocation.frequency).isEqualTo("Weekly")
        assertThat(allocation.startTime.toString()).isEqualTo("09:00")
        assertThat(allocation.endTime.toString()).isEqualTo("16:00")

        assertThat(response.appointments).isNotEmpty
    }

    @Test
    fun `returns 404 when CRN does not exist`() {
        mockMvc.get("/case/X999999/event/1/appointments/schedule") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).contains("Person with crn of X999999 not found")
            }
    }

    @Test
    fun `returns 404 when event number does not exist for person`() {
        mockMvc.get("/case/${PersonGenerator.DEFAULT_PERSON.crn}/event/999/appointments/schedule") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).contains("Event with event number of 999 not found")
            }
    }

    @Test
    fun `returns 200 when summary requested`() {
        mockMvc.get("/case/${PersonGenerator.DEFAULT_PERSON.crn}/summary?username=${UserGenerator.DEFAULT_USER.username}") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                            "case": {
                                "crn": "Z000001",
                                "name": {
                                    "forename": "Default",
                                    "middleNames": [
                                    ],
                                    "surname": "Person"
                                },
                                "dateOfBirth": "1990-06-10",
                                "currentExclusion": false,
                                "currentRestriction": false
                            },
                            "unpaidWorkDetails": [
                                {
                                    "eventNumber": 1,
                                    "sentenceDate": "${UPWGenerator.DISPOSAL_1.date}",
                                    "requiredMinutes": 7200,
                                    "adjustments": 4,
                                    "completedMinutes": 870,
                                    "completedEteMinutes": 0,
                                    "eventOutcome": "Community Order",
                                    "referralDate": "${UPWGenerator.EVENT_1.referralDate}",
                                    "convictionDate": "${UPWGenerator.EVENT_1.convictionDate}",
                                    "court": {
                                        "code": "C01",
                                        "description": "Westminster Magistrates Court"
                                    },
                                    "mainOffence": {
                                        "date": "${UPWGenerator.EVENT_1_MAIN_OFFENCE.offenceDate}",
                                        "count": 1,
                                        "code": "12345",
                                        "description": "Theft from a motor vehicle"
                                    }
                                },
                                {
                                    "eventNumber": 2,
                                    "sentenceDate": "${UPWGenerator.DISPOSAL_2.date}",
                                    "requiredMinutes": 10800,
                                    "adjustments": 0,
                                    "completedMinutes": 405,
                                    "completedEteMinutes": 405,
                                    "eventOutcome": "Community Order",
                                    "referralDate": "${UPWGenerator.EVENT_2.referralDate}",
                                    "convictionDate": "${UPWGenerator.EVENT_2.convictionDate}",
                                    "court": {
                                        "code": "C01",
                                        "description": "Westminster Magistrates Court"
                                    },
                                    "mainOffence": {
                                        "date": "${UPWGenerator.EVENT_2_MAIN_OFFENCE.offenceDate}",
                                        "count": 1,
                                        "code": "12345",
                                        "description": "Theft from a motor vehicle"
                                    }
                                },
                                {
                                    "eventNumber": 3,
                                    "sentenceDate": "${UPWGenerator.DISPOSAL_3.date}",
                                    "requiredMinutes": 0,
                                    "adjustments": 0,
                                    "completedMinutes": 0,
                                    "completedEteMinutes": 0,
                                    "eventOutcome": "Community Order",
                                    "referralDate": "${UPWGenerator.EVENT_3.referralDate}",
                                    "convictionDate": "${UPWGenerator.EVENT_3.convictionDate}",
                                    "court": {
                                        "code": "C01",
                                        "description": "Westminster Magistrates Court"
                                    },
                                    "mainOffence": {
                                        "date": "${UPWGenerator.EVENT_3_MAIN_OFFENCE.offenceDate}",
                                        "count": 1,
                                        "code": "12345",
                                        "description": "Theft from a motor vehicle"
                                    }
                                },
                                {
                                    "eventNumber": 4,
                                    "sentenceDate": "${UPWGenerator.DISPOSAL_4.date}",
                                    "requiredMinutes": 600000,
                                    "adjustments": 0,
                                    "completedMinutes": 0,
                                    "completedEteMinutes": 0,
                                    "eventOutcome": "Community Order",
                                    "upwStatus": "Unallocated",
                                    "referralDate": "${UPWGenerator.EVENT_4.referralDate}",
                                    "convictionDate": "${UPWGenerator.EVENT_4.convictionDate}",
                                    "court": {
                                        "code": "C01",
                                        "description": "Westminster Magistrates Court"
                                    },
                                    "mainOffence": {
                                        "date": "${UPWGenerator.EVENT_4_MAIN_OFFENCE.offenceDate}",
                                        "count": 1,
                                        "code": "12345",
                                        "description": "Theft from a motor vehicle"
                                    }
                                },
                                {
                                    "eventNumber": 5,
                                    "sentenceDate": "${UPWGenerator.DISPOSAL_5.date}",
                                    "requiredMinutes": 0,
                                    "adjustments": 0,
                                    "completedMinutes": 0,
                                    "completedEteMinutes": 0,
                                    "eventOutcome": "Community Order",
                                    "upwStatus": "Working",
                                    "referralDate": "${UPWGenerator.EVENT_5.referralDate}",
                                    "convictionDate": "${UPWGenerator.EVENT_5.convictionDate}",
                                    "court": {
                                        "code": "C01",
                                        "description": "Westminster Magistrates Court"
                                    },
                                    "mainOffence": {
                                        "date": "${UPWGenerator.EVENT_5_MAIN_OFFENCE.offenceDate}",
                                        "count": 1,
                                        "code": "12345",
                                        "description": "Theft from a motor vehicle"
                                    }
                                }
                            ]
                        }
                        """.trimIndent(), JsonCompareMode.STRICT
                    )
                }
            }.andReturn().response.contentAsString
    }

    @Test
    fun `returns 404 for unknown person`() {
        mockMvc.get("/case/X999999/summary?username=${UserGenerator.DEFAULT_USER.username}") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).contains("Person with crn of X999999 not found")
            }
    }
}
