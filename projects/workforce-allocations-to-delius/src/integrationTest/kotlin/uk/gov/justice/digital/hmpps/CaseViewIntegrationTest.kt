package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.CaseView
import uk.gov.justice.digital.hmpps.api.model.CvDocument
import uk.gov.justice.digital.hmpps.api.model.CvOffence
import uk.gov.justice.digital.hmpps.api.model.CvRequirement
import uk.gov.justice.digital.hmpps.api.model.CvSentence
import uk.gov.justice.digital.hmpps.api.model.name
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import java.time.LocalDate

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CaseViewIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @Test
    fun `get case view unauthorised`() {
        mockMvc.perform(
            get("/allocation-demand/N452321/1/case-view")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `get case view no matching crn`() {

        mockMvc.perform(
            get("/allocation-demand/N452321/1/case-view")
                .withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `get case view no matching event`() {

        mockMvc.perform(
            get("/allocation-demand/${PersonGenerator.DEFAULT.crn}/107/case-view")
                .withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `get case view successful`() {
        val person = PersonGenerator.DEFAULT
        val eventNumber = EventGenerator.DEFAULT.number

        val res = mockMvc.perform(
            get("/allocation-demand/${person.crn}/$eventNumber/case-view")
                .withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsString

        val cv = objectMapper.readValue<CaseView>(res)
        Assertions.assertNotNull(cv)
        assertThat(cv.name, equalTo(person.name()))
        assertThat(cv.dateOfBirth, equalTo(person.dateOfBirth))
        assertThat(cv.gender, equalTo("Male"))
        assertThat(cv.mainAddress?.streetName, equalTo(AddressGenerator.DEFAULT.streetName))
        assertThat(cv.mainAddress?.postcode, equalTo(AddressGenerator.DEFAULT.postcode))
        assertThat(cv.mainAddress?.startDate, equalTo(AddressGenerator.DEFAULT.startDate))
        assertThat(
            cv.sentence,
            equalTo(
                CvSentence(
                    "Sentenced - In Custody",
                    LocalDate.now().minusDays(2),
                    "12 Months",
                    LocalDate.now().plusDays(7)
                )
            )
        )
        assertThat(
            cv.offences,
            hasItems(
                CvOffence("Main Offence Category", "Main Offence Sub Category"),
                CvOffence("Offence Main Category", "Offence Sub Category")
            )
        )
        assertThat(
            cv.requirements,
            hasItems(
                CvRequirement("Rqmnt Main Category", "Rqmnt Sub Category", "12 Months")
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
