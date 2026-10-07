package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.appointment.UserAppointments
import uk.gov.justice.digital.hmpps.api.model.appointment.UserDiary
import uk.gov.justice.digital.hmpps.data.generator.UserControllerV2Generator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.ZoneOffset

class UserControllerV2IntegrationTest : IntegrationTestBase() {

    @Test
    fun `v2 upcoming appointments filter using the supplied date time`() {
        val res = mockMvc.get("/v2/user/${UserControllerV2Generator.USER.username}/schedule/upcoming") {
            withToken()
            param("dateTime", UserControllerV2Generator.UPCOMING_FILTER_DATE_TIME.toOffsetDateTime().toString())
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<UserDiary>()

        assertThat(res.totalResults, equalTo(2))
        assertThat(res.totalPages, equalTo(1))
        assertThat(
            res.appointments.map { it.id },
            equalTo(
                listOf(
                    UserControllerV2Generator.AFTER_REFERENCE_CONTACT.id,
                    UserControllerV2Generator.NEXT_DAY_CONTACT.id,
                )
            )
        )
        assertThat(res.appointments.first().caseName, equalTo(Name("Vera", "Integration", "Future")))
        assertThat(
            res.appointments.first().latestSentence,
            equalTo(UserControllerV2Generator.DISPOSAL_TYPE.description)
        )
        assertThat(res.appointments.first().location, equalTo(UserControllerV2Generator.OFFICE_LOCATION.description))
    }

    @Test
    fun `v2 upcoming appointments normalise supplied offset before filtering`() {
        val res = mockMvc.get("/v2/user/${UserControllerV2Generator.USER.username}/schedule/upcoming") {
            withToken()
            param(
                "dateTime",
                UserControllerV2Generator.UPCOMING_FILTER_DATE_TIME
                    .withZoneSameInstant(ZoneOffset.ofHours(2))
                    .toOffsetDateTime()
                    .toString()
            )
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<UserDiary>()

        assertThat(
            res.appointments.map { it.id },
            equalTo(
                listOf(
                    UserControllerV2Generator.AFTER_REFERENCE_CONTACT.id,
                    UserControllerV2Generator.NEXT_DAY_CONTACT.id,
                )
            )
        )
    }

    @Test
    fun `v2 upcoming appointments apply optional from and to date filters`() {
        val res = mockMvc.get("/v2/user/${UserControllerV2Generator.USER.username}/schedule/upcoming") {
            withToken()
            param("dateTime", UserControllerV2Generator.UPCOMING_FILTER_DATE_TIME.toOffsetDateTime().toString())
            param("fromDate", "2035-01-15")
            param("toDate", "2035-01-15")
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<UserDiary>()

        assertThat(res.totalResults, equalTo(1))
        assertThat(
            res.appointments.map { it.id },
            equalTo(listOf(UserControllerV2Generator.AFTER_REFERENCE_CONTACT.id))
        )
    }

    @Test
    fun `v2 upcoming appointments apply from date without requiring a to date`() {
        val res = mockMvc.get("/v2/user/${UserControllerV2Generator.USER.username}/schedule/upcoming") {
            withToken()
            param("dateTime", UserControllerV2Generator.UPCOMING_FILTER_DATE_TIME.toOffsetDateTime().toString())
            param("fromDate", "2035-01-16")
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<UserDiary>()

        assertThat(res.totalResults, equalTo(1))
        assertThat(
            res.appointments.map { it.id },
            equalTo(listOf(UserControllerV2Generator.NEXT_DAY_CONTACT.id))
        )
    }

    @Test
    fun `v2 upcoming appointments apply to date without requiring a from date`() {
        val res = mockMvc.get("/v2/user/${UserControllerV2Generator.USER.username}/schedule/upcoming") {
            withToken()
            param("dateTime", UserControllerV2Generator.UPCOMING_FILTER_DATE_TIME.toOffsetDateTime().toString())
            param("toDate", "2035-01-15")
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<UserDiary>()

        assertThat(res.totalResults, equalTo(1))
        assertThat(
            res.appointments.map { it.id },
            equalTo(listOf(UserControllerV2Generator.AFTER_REFERENCE_CONTACT.id))
        )
    }

    @Test
    fun `v2 no outcome appointments use dedicated isolated data only`() {
        val res = mockMvc.get("/v2/user/${UserControllerV2Generator.USER.username}/schedule/no-outcome") {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<UserDiary>()

        assertThat(res.totalResults, equalTo(1))
        assertThat(
            res.appointments.map { it.id },
            equalTo(
                listOf(
                    UserControllerV2Generator.HISTORIC_CONTACT.id,
                )
            )
        )
        assertThat(
            res.appointments.map { it.type }.distinct(),
            equalTo(listOf(UserControllerV2Generator.CONTACT_TYPE.description))
        )
    }

    @Test
    fun `v2 no outcome appointments apply optional from and to date filters`() {
        val res = mockMvc.get("/v2/user/${UserControllerV2Generator.USER.username}/schedule/no-outcome") {
            withToken()
            param("fromDate", "2025-01-01")
            param("toDate", "2025-12-31")
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<UserDiary>()

        assertThat(res.totalResults, equalTo(1))
        assertThat(res.appointments.map { it.id }, equalTo(listOf(UserControllerV2Generator.HISTORIC_CONTACT.id)))
    }

    @Test
    fun `v2 no outcome appointments apply from date without requiring a to date`() {
        val res = mockMvc.get("/v2/user/${UserControllerV2Generator.USER.username}/schedule/no-outcome") {
            withToken()
            param("fromDate", "2025-01-10")
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<UserDiary>()

        assertThat(res.totalResults, equalTo(1))
        assertThat(res.appointments.map { it.id }, equalTo(listOf(UserControllerV2Generator.HISTORIC_CONTACT.id)))
    }

    @Test
    fun `v2 no outcome appointments apply to date without requiring a from date`() {
        val res = mockMvc.get("/v2/user/${UserControllerV2Generator.USER.username}/schedule/no-outcome") {
            withToken()
            param("toDate", "2025-01-09")
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<UserDiary>()

        assertThat(res.totalResults, equalTo(0))
        assertThat(res.appointments, equalTo(emptyList()))
    }

    @Test
    fun `v2 appointments summary combines upcoming and no outcome results`() {
        val res = mockMvc.get("/v2/user/${UserControllerV2Generator.USER.username}/appointments") {
            withToken()
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<UserAppointments>()

        val expectedUpcomingIds = listOf(
            UserControllerV2Generator.BEFORE_REFERENCE_CONTACT.id,
            UserControllerV2Generator.EXACT_REFERENCE_CONTACT.id,
            UserControllerV2Generator.AFTER_REFERENCE_CONTACT.id,
            UserControllerV2Generator.NEXT_DAY_CONTACT.id,
        )

        assertThat(res.staff, equalTo(Name("Veronica", null, "Tester")))
        assertThat(res.totalAppointments, equalTo(4))
        assertThat(res.totalOutcomes, equalTo(1))
        assertThat(res.appointments.map { it.id }, equalTo(expectedUpcomingIds))
        assertThat(res.outcomes.map { it.id }, equalTo(listOf(UserControllerV2Generator.HISTORIC_CONTACT.id)))
    }

    @Test
    fun `v2 appointments summary applies optional from and to date filters`() {
        val res = mockMvc.get("/v2/user/${UserControllerV2Generator.USER.username}/appointments") {
            withToken()
            param("fromDate", "2035-01-15")
            param("toDate", "2035-01-15")
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<UserAppointments>()

        assertThat(res.totalAppointments, equalTo(3))
        assertThat(res.totalOutcomes, equalTo(0))
        assertThat(
            res.appointments.map { it.id },
            equalTo(
                listOf(
                    UserControllerV2Generator.BEFORE_REFERENCE_CONTACT.id,
                    UserControllerV2Generator.EXACT_REFERENCE_CONTACT.id,
                    UserControllerV2Generator.AFTER_REFERENCE_CONTACT.id,
                )
            )
        )
        assertThat(res.outcomes, equalTo(emptyList()))
    }

    @Test
    fun `v2 appointments summary applies from date without requiring a to date`() {
        val res = mockMvc.get("/v2/user/${UserControllerV2Generator.USER.username}/appointments") {
            withToken()
            param("fromDate", "2035-01-16")
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<UserAppointments>()

        assertThat(res.totalAppointments, equalTo(1))
        assertThat(res.totalOutcomes, equalTo(0))
        assertThat(res.appointments.map { it.id }, equalTo(listOf(UserControllerV2Generator.NEXT_DAY_CONTACT.id)))
        assertThat(res.outcomes, equalTo(emptyList()))
    }

    @Test
    fun `v2 appointments summary applies to date without requiring a from date`() {
        val res = mockMvc.get("/v2/user/${UserControllerV2Generator.USER.username}/appointments") {
            withToken()
            param("toDate", "2025-12-31")
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<UserAppointments>()

        assertThat(res.totalAppointments, equalTo(0))
        assertThat(res.totalOutcomes, equalTo(1))
        assertThat(res.appointments, equalTo(emptyList()))
        assertThat(res.outcomes.map { it.id }, equalTo(listOf(UserControllerV2Generator.HISTORIC_CONTACT.id)))
    }

    @Test
    fun `v2 upcoming appointments require date time parameter`() {
        mockMvc.get("/v2/user/${UserControllerV2Generator.USER.username}/schedule/upcoming") {
            withToken()
        }
            .andExpect { status { isBadRequest() } }
    }

    @Test
    fun `v2 appointments returns not found for unknown user`() {
        mockMvc.get("/v2/user/unknown-user/appointments") {
            withToken()
        }
            .andExpect { status { isNotFound() } }
    }
}



