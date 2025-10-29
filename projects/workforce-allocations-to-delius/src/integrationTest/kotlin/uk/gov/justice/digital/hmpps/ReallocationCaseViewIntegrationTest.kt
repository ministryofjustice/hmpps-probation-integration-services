package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.isNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.CvOffence
import uk.gov.justice.digital.hmpps.api.model.CvRequirement
import uk.gov.justice.digital.hmpps.api.model.CvSentence
import uk.gov.justice.digital.hmpps.api.model.ReallocationCaseView
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.time.ZonedDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ReallocationCaseViewIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `get reallocation case view unauthorised`() {
        mockMvc.perform(get("/reallocation/N452321/case-view"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `get reallocation case view no matching crn`() {
        mockMvc.perform(get("/reallocation/N452321/case-view").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `get reallocation case view successful`() {
        val person = PersonGenerator.CASE_VIEW

        val cv = mockMvc.perform(get("/reallocation/${person.crn}/case-view").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<ReallocationCaseView>()

        Assertions.assertNotNull(cv)
        assertThat(cv.name.forename, equalTo(person.forename))
        assertThat(cv.name.surname, equalTo(person.surname))
        assertThat(cv.dateOfBirth, equalTo(person.dateOfBirth))
        assertThat(cv.gender, equalTo("Male"))
        assertThat(cv.mainAddress?.streetName, equalTo(AddressGenerator.CASE_VIEW.streetName))
        assertThat(cv.mainAddress?.postcode, equalTo(AddressGenerator.CASE_VIEW.postcode))
        assertThat(cv.mainAddress?.startDate, equalTo(AddressGenerator.CASE_VIEW.startDate))
        assertThat(cv.nextAppointmentDate, equalTo(LocalDate.now().plusDays(1)))
        with(cv.activeEvents.first()) {
            assertThat(failureToComplyCount, equalTo(2))
            assertThat(
                sentence,
                equalTo(
                    CvSentence(
                        "Case View Sentence Type",
                        LocalDate.now().minusDays(2),
                        "12 Months",
                        LocalDate.now().plusDays(7)
                    )
                )
            )
            assertThat(
                offences,
                hasItems(
                    CvOffence("Main Offence Category", "Main Offence Sub Category", true),
                    CvOffence("Offence Main Category", "Offence Sub Category", false)
                )
            )
            assertThat(
                requirements,
                hasItems(
                    CvRequirement("Main Category for Case View", "Rqmnt Sub Category", "12 Months")
                )
            )
        }
    }
}
