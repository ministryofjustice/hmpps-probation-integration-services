package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.service.entity.Disposal
import uk.gov.justice.digital.hmpps.service.entity.Person
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.ZoneId
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class SentencePlanIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `API call retuns a success response`() {

        mockMvc
            .perform(get("/case-details/X123123").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(
                getDetailResponse(
                    true,
                    PersonGenerator.DEFAULT,
                    EventGenerator.DEFAULT_DISPOSAL,
                    0,
                    20,
                    2,
                    1
                )
            )
    }

    @Test
    fun `API call retuns a non custody person`() {
        mockMvc
            .perform(get("/case-details/X123124").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(
                getDetailResponse(
                    false,
                    PersonGenerator.NON_CUSTODIAL,
                    EventGenerator.NON_CUSTODIAL_DISPOSAL,
                    6,
                    3,
                    0,
                    0
                )
            )
    }

    @Test
    fun `API call to get first appointment date`() {
        val date = ZonedDateTime.of(2020, 12, 1, 12, 12, 12, 0, ZoneId.of("Europe/London"))
        mockMvc
            .perform(get("/case-details/X123124/first-appointment-date").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(FirstAppointment(date))
    }

    private fun getDetailResponse(
        custody: Boolean = true, person: Person, disposal: Disposal,
        upwHoursOrdered: Int, upwMinutesCompleted: Int,
        rarDaysOrdered: Int, rarDaysCompleted: Int,
    ): CaseDetails {
        return CaseDetails(
            Name(
                person.forename,
                person.secondName,
                person.surname
            ),
            person.crn,
            person.tier!!.description,
            person.dateOfBirth,
            person.nomisId,
            ProviderGenerator.DEFAULT_AREA.description,
            Manager(
                Name(
                    ProviderGenerator.DEFAULT_STAFF.forename,
                    ProviderGenerator.DEFAULT_STAFF.middleName,
                    ProviderGenerator.DEFAULT_STAFF.surname
                ),
                ProviderGenerator.DEFAULT_STAFF.isUnallocated()
            ),
            custody,
            listOf(
                Sentence(
                    disposal.type.description, disposal.startDate, disposal.expectedEndDate(), false,
                    upwHoursOrdered, upwMinutesCompleted, rarDaysOrdered, rarDaysCompleted
                )
            )
        )
    }
}
