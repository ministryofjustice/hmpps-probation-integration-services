package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.common.ContentTypes.APPLICATION_JSON
import com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE
import io.jsonwebtoken.Jwts
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.appointment.AppointmentCheck
import uk.gov.justice.digital.hmpps.api.model.appointment.AppointmentChecks
import uk.gov.justice.digital.hmpps.api.model.appointment.CheckAppointment
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.USER
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.STAFF_USER_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.time.Duration
import java.time.ZonedDateTime
import java.util.*

class CheckAppointmentIntegrationTest : IntegrationTestBase() {

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
                    isWithinOneHourOfMeetingWith = AppointmentCheck(
                        appointmentIsWith = Name(
                            forename = "John",
                            surname = "Smith"
                        ), isCurrentUser = false, startAndEnd = "9am to 10am"
                    ),
                    overlapsWithMeetingWith = null
                ), false, STAFF_USER_1.username
            ),
            Arguments.of(
                named(
                    "the appointment is within 1 hour before an existing meeting at 9:00 to 10:00",
                    ZonedDateTime.of(2024, 11, 27, 10, 15, 0, 0, EuropeLondon)
                ),
                ZonedDateTime.of(2024, 11, 27, 11, 0, 0, 0, EuropeLondon),
                AppointmentChecks(
                    nonWorkingDayName = null,
                    isWithinOneHourOfMeetingWith = AppointmentCheck(
                        appointmentIsWith = Name(
                            forename = "John",
                            surname = "Smith"
                        ), isCurrentUser = false, startAndEnd = "9am to 10am"
                    ),
                    overlapsWithMeetingWith = null
                ), false, STAFF_USER_1.username
            ),
            Arguments.of(
                named(
                    "the appointment is within 1 hour after an existing meeting at 9:00 to 10:00",
                    ZonedDateTime.of(2024, 11, 27, 10, 45, 0, 0, EuropeLondon)
                ),
                ZonedDateTime.of(2024, 11, 27, 11, 15, 0, 0, EuropeLondon),
                AppointmentChecks(
                    nonWorkingDayName = null,
                    isWithinOneHourOfMeetingWith = AppointmentCheck(
                        appointmentIsWith = Name(
                            forename = "John",
                            surname = "Smith"
                        ), isCurrentUser = false, startAndEnd = "9am to 10am"
                    ),
                    overlapsWithMeetingWith = null
                ), false, STAFF_USER_1.username
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
                    overlapsWithMeetingWith = AppointmentCheck(
                        appointmentIsWith = Name(
                            forename = "John",
                            surname = "Smith"
                        ), isCurrentUser = false, startAndEnd = "9am to 10am"
                    ),
                ), false, STAFF_USER_1.username
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
                    overlapsWithMeetingWith = AppointmentCheck(
                        appointmentIsWith = Name(
                            forename = "John",
                            surname = "Smith"
                        ), isCurrentUser = false, startAndEnd = "9am to 10am"
                    ),
                ), false, STAFF_USER_1.username
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
                    overlapsWithMeetingWith = AppointmentCheck(
                        appointmentIsWith = Name(
                            forename = "John",
                            surname = "Smith"
                        ), isCurrentUser = false, startAndEnd = "9am to 10am"
                    ),
                ), false, STAFF_USER_1.username
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
                ), false, STAFF_USER_1.username
            ),
            Arguments.of(
                named(
                    "the appointment is inside of an hour (1 min) before the start of the existing meeting at 9:00 to 10:00",
                    ZonedDateTime.of(2024, 11, 27, 7, 1, 0, 0, EuropeLondon)
                ),
                ZonedDateTime.of(2024, 11, 27, 8, 1, 0, 0, EuropeLondon),
                AppointmentChecks(
                    nonWorkingDayName = null,
                    isWithinOneHourOfMeetingWith = AppointmentCheck(
                        appointmentIsWith = Name(
                            forename = "John",
                            surname = "Smith"
                        ), isCurrentUser = true, startAndEnd = "9am to 10am"
                    ),
                    overlapsWithMeetingWith = null
                ), false, USER.username
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
                ), false, STAFF_USER_1.username
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
                ), false, STAFF_USER_1.username
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
                ), true, STAFF_USER_1.username
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
                ), true, STAFF_USER_1.username
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
                ), true, STAFF_USER_1.username
            ),
            Arguments.of(
                named(
                    "the appointment is close to the end of the day",
                    ZonedDateTime.of(2024, 11, 27, 22, 45, 0, 0, EuropeLondon)
                ),
                ZonedDateTime.of(2024, 11, 27, 23, 15, 0, 0, EuropeLondon),
                AppointmentChecks(
                    nonWorkingDayName = null,
                    isWithinOneHourOfMeetingWith = null,
                    overlapsWithMeetingWith = AppointmentCheck(
                        isCurrentUser = false,
                        appointmentIsWith = Name(
                            forename = "John",
                            surname = "Smith"
                        ),
                        startAndEnd = "11pm to 11:30pm"
                    )
                ), true, STAFF_USER_1.username
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
        forceFailBankHolidayCall: Boolean = false,
        username: String,
    ) {
        if (forceFailBankHolidayCall) {
            withJsonResponse("/gov-uk/bank-holidays.json", "error.json", 500)
        } else {
            withJsonResponse("/gov-uk/bank-holidays.json", "bank-holidays.json", 200)
        }

        val response = mockMvc.perform(
            post("/appointment/${PersonGenerator.PERSON_1.crn}/check")
                .withUserToken(username)
                .withJson(CheckAppointment(start, end))
        ).andExpect(status().isOk).andReturn().response.contentAsJson<AppointmentChecks>()
        assertThat(response, equalTo(expected))
    }
}

fun createToken(username: String): String {

    val keyPair: KeyPair = KeyPairGenerator.getInstance("RSA").apply { this.initialize(2048) }.generateKeyPair()
    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject("probation-integration-dev")
        .claim("user_name", username)
        .claim("sub", "probation-integration-dev")
        .claim("authorities", listOf("ROLE_PROBATION_INTEGRATION_ADMIN"))
        .expiration(Date(System.currentTimeMillis() + Duration.ofHours(1L).toMillis()))
        .signWith(keyPair.private, Jwts.SIG.RS256)
        .compact()
}

fun MockHttpServletRequestBuilder.withUserToken(username: String) =
    header(HttpHeaders.AUTHORIZATION, "Bearer ${createToken(username)}")