package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CaseViewIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `get case view unauthorised`() {
        mockMvc.perform(get("/allocation-demand/N452321/1/case-view"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `get case view no matching crn`() {
        mockMvc.perform(get("/allocation-demand/N452321/1/case-view").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `get case view no matching event`() {
        mockMvc.perform(get("/allocation-demand/${PersonGenerator.DEFAULT.crn}/107/case-view").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `get case view successful`() {
        val person = PersonGenerator.CASE_VIEW
        val eventNumber = EventGenerator.CASE_VIEW.number

        val cv = mockMvc.perform(get("/allocation-demand/${person.crn}/$eventNumber/case-view").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<CaseView>()

        Assertions.assertNotNull(cv)
        assertThat(cv.name.forename, equalTo(person.forename))
        assertThat(cv.name.surname, equalTo(person.surname))
        assertThat(cv.dateOfBirth, equalTo(person.dateOfBirth))
        assertThat(cv.gender, equalTo("Male"))
        assertThat(cv.mainAddress?.streetName, equalTo(AddressGenerator.CASE_VIEW.streetName))
        assertThat(cv.mainAddress?.postcode, equalTo(AddressGenerator.CASE_VIEW.postcode))
        assertThat(cv.mainAddress?.startDate, equalTo(AddressGenerator.CASE_VIEW.startDate))
        assertThat(
            cv.sentence,
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
            cv.offences,
            hasItems(
                CvOffence("Main Offence Category", "Main Offence Sub Category", true),
                CvOffence("Offence Main Category", "Offence Sub Category", false)
            )
        )
        assertThat(
            cv.requirements,
            hasItems(
                CvRequirement("Main Category for Case View", "Rqmnt Sub Category", "12 Months")
            )
        )
        assertThat(
            cv.courtReport,
            equalTo(
                CvDocument(
                    DocumentGenerator.COURT_REPORT.alfrescoId!!,
                    DocumentGenerator.COURT_REPORT.name,
                    DocumentGenerator.COURT_REPORT.dateProduced!!.toLocalDate(),
                    DocumentGenerator.COURT_REPORT.findRelatedTo().description
                )
            )
        )
        assertThat(
            cv.preConvictionDocument,
            equalTo(
                CvDocument(
                    DocumentGenerator.PREVIOUS_CONVICTION.alfrescoId!!,
                    DocumentGenerator.PREVIOUS_CONVICTION.name,
                    DocumentGenerator.PREVIOUS_CONVICTION.dateProduced!!.toLocalDate(),
                    DocumentGenerator.PREVIOUS_CONVICTION.findRelatedTo().description
                )
            )
        )
        assertThat(
            cv.cpsPack,
            equalTo(
                CvDocument(
                    DocumentGenerator.CPS_PACK.alfrescoId!!,
                    DocumentGenerator.CPS_PACK.name,
                    DocumentGenerator.CPS_PACK.lastSaved!!.toLocalDate(),
                    DocumentGenerator.CPS_PACK.findRelatedTo().description
                )
            )
        )
    }
}
