package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.common.ContentTypes.APPLICATION_JSON
import com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.appointment.AppointmentChecks
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.api.model.appointment.User
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.STAFF_USER_1
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.TEAM
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.ZonedDateTime
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CheckAppointmentIntegrationTest {

    @Autowired
    internal lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    private val user = User(STAFF_USER_1.username, TEAM.code)

    private val person = PersonGenerator.PERSON_1

    companion object {
        @JvmStatic
        fun appointmentChecks(): List<Arguments> = listOf(
            Arguments.of(
                named(
                    "the appointment is within 1 hour before an existing meeting at 9:00 to 10:00",
                    ZonedDateTime.of(2024, 11, 27, 8, 30, 0, 0, EuropeLondon)
                ),
                ZonedDateTime.of(2024, 11, 27, 8, 45, 0, 0, EuropeLondon),
                AppointmentChecks(
                    nonWorkingDayName = null,
                    isWithinOneHourOfMeetingWith = Name(forename = "John", surname = "Smith"),
                    overlapsWithMeetingWith = null
                ), false
            ),
            Arguments.of(
                named(
                    "the appointment is within 1 hour before an existing meeting at 9:00 to 10:00",
                    ZonedDateTime.of(2024, 11, 27, 10, 15, 0, 0, EuropeLondon)
                ),
                ZonedDateTime.of(2024, 11, 27, 11, 0, 0, 0, EuropeLondon),
                AppointmentChecks(
                    nonWorkingDayName = null,
                    isWithinOneHourOfMeetingWith = Name(forename = "John", surname = "Smith"),
                    overlapsWithMeetingWith = null
                ), false
            ),
            Arguments.of(
                named(
                    "the appointment is within 1 hour after an existing meeting at 9:00 to 10:00",
                    ZonedDateTime.of(2024, 11, 27, 10, 45, 0, 0, EuropeLondon)
                ),
                ZonedDateTime.of(2024, 11, 27, 11, 15, 0, 0, EuropeLondon),
                AppointmentChecks(
                    nonWorkingDayName = null,
                    isWithinOneHourOfMeetingWith = Name(forename = "John", surname = "Smith"),
                    overlapsWithMeetingWith = null
                ), false
            ),
            Arguments.of(
                named(
                    "the appointment is the same time as an existing meeting at 9:00 to 10:00",
                    ZonedDateTime.of(2024, 11, 27, 9, 0, 0, 0, EuropeLondon)
                ),
                ZonedDateTime.of(2024, 11, 27, 10, 0, 0, 0, EuropeLondon),
                AppointmentChecks(
                    nonWorkingDayName = null,
                    isWithinOneHourOfMeetingWith = null,
                    overlapsWithMeetingWith = Name(forename = "John", surname = "Smith")
                ), false
            ),
            Arguments.of(
                named(
                    "the appointment is overlaps the start of the existing meeting at 9:00 to 10:00",
                    ZonedDateTime.of(2024, 11, 27, 8, 45, 0, 0, EuropeLondon)
                ),
                ZonedDateTime.of(2024, 11, 27, 9, 15, 0, 0, EuropeLondon),
                AppointmentChecks(
                    nonWorkingDayName = null,
                    isWithinOneHourOfMeetingWith = null,
                    overlapsWithMeetingWith = Name(forename = "John", surname = "Smith")
                ), false
            ),
            Arguments.of(
                named(
                    "the appointment is overlaps the end of the existing meeting at 9:00 to 10:00",
                    ZonedDateTime.of(2024, 11, 27, 9, 45, 0, 0, EuropeLondon)
                ),
                ZonedDateTime.of(2024, 11, 27, 10, 15, 0, 0, EuropeLondon),
                AppointmentChecks(
                    nonWorkingDayName = null,
                    isWithinOneHourOfMeetingWith = null,
                    overlapsWithMeetingWith = Name(forename = "John", surname = "Smith")
                ), false
            ),
            Arguments.of(
                named(
                    "the appointment is outside of an hour before the start of the existing meeting at 9:00 to 10:00",
                    ZonedDateTime.of(2024, 11, 27, 7, 0, 0, 0, EuropeLondon)
                ),
                ZonedDateTime.of(2024, 11, 27, 8, 0, 0, 0, EuropeLondon),
                AppointmentChecks(
                    nonWorkingDayName = null,
                    isWithinOneHourOfMeetingWith = null,
                    overlapsWithMeetingWith = null
                ), false
            ),
            Arguments.of(
                named(
                    "the appointment is inside of an hour (1 min) before the start of the existing meeting at 9:00 to 10:00",
                    ZonedDateTime.of(2024, 11, 27, 7, 1, 0, 0, EuropeLondon)
                ),
                ZonedDateTime.of(2024, 11, 27, 8, 1, 0, 0, EuropeLondon),
                AppointmentChecks(
                    nonWorkingDayName = null,
                    isWithinOneHourOfMeetingWith = Name(forename = "John", surname = "Smith"),
                    overlapsWithMeetingWith = null
                ), false
            ),
            Arguments.of(
                named(
                    "the appointment is outside of an hour before the start of the existing meeting at 9:00 to 10:00",
                    ZonedDateTime.of(2024, 11, 27, 11, 0, 0, 0, EuropeLondon)
                ),
                ZonedDateTime.of(2024, 11, 27, 12, 0, 0, 0, EuropeLondon),
                AppointmentChecks(
                    nonWorkingDayName = null,
                    isWithinOneHourOfMeetingWith = null,
                    overlapsWithMeetingWith = null
                ), false
            ),
            Arguments.of(
                named(
                    "the appointment is a non working day",
                    ZonedDateTime.of(2026, 5, 4, 11, 0, 0, 0, EuropeLondon)
                ),
                ZonedDateTime.of(2024, 11, 27, 12, 0, 0, 0, EuropeLondon),
                AppointmentChecks(
                    nonWorkingDayName = "Early May bank holiday",
                    isWithinOneHourOfMeetingWith = null,
                    overlapsWithMeetingWith = null
                ), false
            ),
            Arguments.of(
                named(
                    "the bank holiday service call fails",
                    ZonedDateTime.of(2026, 5, 4, 11, 0, 0, 0, EuropeLondon)
                ),
                ZonedDateTime.of(2024, 11, 27, 12, 0, 0, 0, EuropeLondon),
                AppointmentChecks(
                    nonWorkingDayName = null,
                    isWithinOneHourOfMeetingWith = null,
                    overlapsWithMeetingWith = null
                ), true
            ),
            Arguments.of(
                named(
                    "the appointment is on a Saturday",
                    ZonedDateTime.of(2025, 6, 14, 11, 0, 0, 0, EuropeLondon)
                ),
                ZonedDateTime.of(2025, 6, 14, 12, 0, 0, 0, EuropeLondon),
                AppointmentChecks(
                    nonWorkingDayName = "Saturday",
                    isWithinOneHourOfMeetingWith = null,
                    overlapsWithMeetingWith = null
                ), true
            ),
            Arguments.of(
                named(
                    "the appointment is on a Sunday",
                    ZonedDateTime.of(2025, 6, 15, 11, 0, 0, 0, EuropeLondon)
                ),
                ZonedDateTime.of(2025, 6, 15, 12, 0, 0, 0, EuropeLondon),
                AppointmentChecks(
                    nonWorkingDayName = "Sunday",
                    isWithinOneHourOfMeetingWith = null,
                    overlapsWithMeetingWith = null
                ), true
            )
        )
    }

    private fun withJsonResponse(url: String, filename: String, status: Int) {
        val response = aResponse().withStatus(status).withBodyFile(filename).withHeader(CONTENT_TYPE, APPLICATION_JSON)
        wireMockServer.addStubMapping(get(url).willReturn(response).build())
    }

    @ParameterizedTest
    @MethodSource("appointmentChecks")
    fun `appointment checks are performed`(
        start: ZonedDateTime,
        end: ZonedDateTime,
        expected: AppointmentChecks,
        forceFailBankHolidayCall: Boolean = false
    ) {
        if (forceFailBankHolidayCall) {
            withJsonResponse("/gov-uk/bank-holidays.json", "error.json", 500)
        } else {
            withJsonResponse("/gov-uk/bank-holidays.json", "bank-holidays.json", 200)
        }
        val response = mockMvc.perform(
            post("/appointment/${person.crn}/check")
                .withToken()
                .withJson(
                    CreateAppointment(
                        user,
                        CreateAppointment.Type.InitialAppointmentInOfficeNS.code,
                        start,
                        end,
                        interval = CreateAppointment.Interval.DAY,
                        numberOfAppointments = 1,
                        PersonGenerator.EVENT_1.id,
                        UUID.randomUUID()
                    )
                )
        ).andExpect(status().isOk).andReturn().response.contentAsJson<AppointmentChecks>()
        assertThat(response, equalTo(expected))
    }
}