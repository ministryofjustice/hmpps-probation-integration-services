package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.controller.ResultSet
import uk.gov.justice.digital.hmpps.api.model.Appointment
import uk.gov.justice.digital.hmpps.api.model.DutyToReferNSI
import uk.gov.justice.digital.hmpps.api.model.Officer
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.service.toModel
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `duty to refer retuns a success response`() {
        val detailResponse = mockMvc
            .perform(get("/duty-to-refer-nsi/X123123?type=CRN").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<DutyToReferNSI>()

        assertThat(detailResponse).isEqualTo(getNSI())
    }

    private fun getNSI() = DutyToReferNSI(
        ReferenceDataGenerator.DTR_SUB_TYPE.description,
        NSIGenerator.DEFAULT.referralDate,
        ProviderGenerator.DEFAULT_AREA.description,
        ProviderGenerator.DEFAULT_TEAM.description,
        Officer(
            ProviderGenerator.DEFAULT_STAFF.forename,
            ProviderGenerator.DEFAULT_STAFF.surname,
            ProviderGenerator.DEFAULT_STAFF.middleName
        ),
        NSIStatusGenerator.INITIATED.description,
        NSIGenerator.DEFAULT.actualStartDate,
        NSIGenerator.DEFAULT.notes,
        AddressGenerator.DEFAULT.toModel()
    )

    @Test
    fun `duty to refer retuns a 404 response`() {
        mockMvc
            .perform(get("/duty-to-refer-nsi/N123123B?type=CRN").withToken())
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `future appointments are retrieved and mapped accordingly`() {
        val res = mockMvc.perform(
            get("/appointments/${PersonGenerator.DEFAULT.crn}")
                .queryParam("size", "10")
                .queryParam("page", "1")
                .queryParam("startDate", LocalDate.now().toString())
                .queryParam("endDate", LocalDate.now().plusDays(7).toString())
                .withToken()
        )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<ResultSet<Appointment>>()

        res.assertPagination()
        res.results.forEach {
            assertThat(it.dateTime, greaterThan(ZonedDateTime.now()))
            assertThat(it.dateTime, lessThan(ZonedDateTime.now().plusDays(7)))
            assertNull(it.outcome)
            it.commonAssertions()
        }
    }

    @Test
    fun `past appointments are retrieved and mapped accordingly`() {
        val res = mockMvc.perform(
            get("/appointments/${PersonGenerator.DEFAULT.crn}")
                .queryParam("size", "10")
                .queryParam("page", "1")
                .queryParam("startDate", LocalDate.now().minusDays(7).toString())
                .queryParam("endDate", LocalDate.now().toString())
                .withToken()
        )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<ResultSet<Appointment>>()

        res.assertPagination()
        res.results.forEach {
            assertThat(it.dateTime, lessThan(ZonedDateTime.now()))
            assertThat(it.dateTime, greaterThan(ZonedDateTime.now().minusDays(7)))
            assertThat(it.outcome?.code, equalTo(AppointmentGenerator.ATTENDED_OUTCOME.code))
            it.commonAssertions()
        }
    }

    private fun ResultSet<Appointment>.assertPagination() {
        assertThat(page, equalTo(1))
        assertThat(size, equalTo(10))
        assertThat(totalPages, equalTo(4))
        assertThat(totalElements, equalTo(40))
        assertThat(results.size, equalTo(10))
    }

    private fun Appointment.commonAssertions() {
        if (dateTime.minute == 0) {
            assertThat(description, equalTo("On the hour"))
        } else if (dateTime.minute == 30) {
            assertThat(location?.code, equalTo(AppointmentGenerator.DEFAULT_LOCATION.code))
        }
        assertThat(staff.code, equalTo(ProviderGenerator.DEFAULT_STAFF.code))
        assertThat(staff.email, equalTo("john.smith@moj.gov.uk"))
        assertThat(staff.telephone, equalTo("07321165373"))
    }
}
