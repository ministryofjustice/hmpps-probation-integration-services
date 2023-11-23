package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.DocumentType
import uk.gov.justice.digital.hmpps.api.model.ProbationRecord
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class OffenderIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `API call retuns probation record`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        val result = mockMvc
            .perform(get("/probation-case/$crn").withOAuth2Token(wireMockServer))
            .andExpect(status().is2xxSuccessful).andReturn()

        val todaysDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val futureDate = LocalDate.now().plusDays(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val detailResponse = objectMapper.readValue(result.response.contentAsString, ProbationRecord::class.java)
        Assertions.assertThat(detailResponse.crn).isEqualTo(crn)
        Assertions.assertThat(detailResponse.offenderManagers[0].staff.forenames).isEqualTo(StaffGenerator.ALLOCATED.forename + " " + StaffGenerator.ALLOCATED.forename2)
        Assertions.assertThat(detailResponse.offenderManagers[0].staff.surname).isEqualTo(StaffGenerator.ALLOCATED.surname)
        Assertions.assertThat(detailResponse.offenderManagers[0].team.description).isEqualTo(TeamGenerator.DEFAULT.description)

        Assertions.assertThat(detailResponse.convictions[0].inBreach).isEqualTo(true)
        Assertions.assertThat(detailResponse.convictions[0].active).isEqualTo(true)
        Assertions.assertThat(detailResponse.convictions[0].awaitingPsr).isEqualTo(false)
        Assertions.assertThat(detailResponse.convictions[0].convictionDate).isEqualTo(todaysDate)

        Assertions.assertThat(detailResponse.convictions[0].offences[0].description).isEqualTo("Main Offence")
        Assertions.assertThat(detailResponse.convictions[0].offences[0].main).isEqualTo(true)
        Assertions.assertThat(detailResponse.convictions[0].offences[0].offenceDate).isEqualTo(todaysDate)
        Assertions.assertThat(detailResponse.convictions[0].offences[1].description).isEqualTo("Additional Offence")
        Assertions.assertThat(detailResponse.convictions[0].offences[1].main).isEqualTo(false)
        Assertions.assertThat(detailResponse.convictions[0].offences[1].offenceDate).isEqualTo(todaysDate)
        Assertions.assertThat(detailResponse.convictions[0].sentence?.description).isEqualTo("Disposal type")
        Assertions.assertThat(detailResponse.convictions[0].sentence?.length).isEqualTo(12)
        Assertions.assertThat(detailResponse.convictions[0].sentence?.lengthUnits).isEqualTo("Days")
        Assertions.assertThat(detailResponse.convictions[0].sentence?.lengthInDays).isEqualTo(99)
        Assertions.assertThat(detailResponse.convictions[0].sentence?.startDate).isEqualTo(todaysDate)

        Assertions.assertThat(detailResponse.convictions[0].custodialType?.code).isEqualTo("C1")
        Assertions.assertThat(detailResponse.convictions[0].custodialType?.description).isEqualTo("Custodial status")

        Assertions.assertThat(detailResponse.convictions[0].documents[0].documentName).isEqualTo("filename.txt")
        Assertions.assertThat(detailResponse.convictions[0].documents[0].type).isEqualTo(DocumentType.CONVICTION_DOCUMENT)
        Assertions.assertThat(detailResponse.convictions[0].documents[0].subType?.code).isEqualTo("EVENT")
        Assertions.assertThat(detailResponse.convictions[0].documents[0].subType?.description).isEqualTo("Sentence related")

        Assertions.assertThat(detailResponse.convictions[0].breaches[0].description).isEqualTo("NSI Type desc")
        Assertions.assertThat(detailResponse.convictions[0].breaches[0].status).isEqualTo("this NSI is in breach")
        Assertions.assertThat(detailResponse.convictions[0].breaches[0].started).isEqualTo(todaysDate)
        Assertions.assertThat(detailResponse.convictions[0].breaches[0].statusDate).isEqualTo(todaysDate)

        Assertions.assertThat(detailResponse.convictions[0].requirements[0].commencementDate).isEqualTo(todaysDate)
        Assertions.assertThat(detailResponse.convictions[0].requirements[0].active).isEqualTo(true)
        Assertions.assertThat(detailResponse.convictions[0].requirements[0].requirementTypeMainCategory?.description).isEqualTo("Main cat")
        Assertions.assertThat(detailResponse.convictions[0].requirements[0].requirementTypeMainCategory?.code).isEqualTo("Main")
        Assertions.assertThat(detailResponse.convictions[0].requirements[0].requirementTypeSubCategory?.description).isEqualTo("Sub cat")
        Assertions.assertThat(detailResponse.convictions[0].requirements[0].requirementTypeSubCategory?.code).isEqualTo("Sub")

        Assertions.assertThat(detailResponse.convictions[0].requirements[0].adRequirementTypeMainCategory?.description).isEqualTo("AdMain cat")
        Assertions.assertThat(detailResponse.convictions[0].requirements[0].adRequirementTypeMainCategory?.code).isEqualTo("AdMain")
        Assertions.assertThat(detailResponse.convictions[0].requirements[0].adRequirementTypeSubCategory?.description).isEqualTo("AdSub cat")
        Assertions.assertThat(detailResponse.convictions[0].requirements[0].adRequirementTypeSubCategory?.code).isEqualTo("AdSub")

        Assertions.assertThat(detailResponse.convictions[0].pssRequirements[0].description).isEqualTo("pss main")
        Assertions.assertThat(detailResponse.convictions[0].pssRequirements[0].subTypeDescription).isEqualTo("pss sub")

        Assertions.assertThat(detailResponse.convictions[0].licenceConditions[0].description).isEqualTo("lic cond main")
        Assertions.assertThat(detailResponse.convictions[0].licenceConditions[0].subTypeDescription).isEqualTo("Lic Sub cat")
        Assertions.assertThat(detailResponse.convictions[0].licenceConditions[0].startDate).isEqualTo(todaysDate)
        Assertions.assertThat(detailResponse.convictions[0].licenceConditions[0].notes).isEqualTo("Licence Condition notes")

        Assertions.assertThat(detailResponse.convictions[0].courtReports[0].requestedDate).isEqualTo(todaysDate)
        Assertions.assertThat(detailResponse.convictions[0].courtReports[0].requiredDate).isEqualTo(futureDate)
        Assertions.assertThat(detailResponse.convictions[0].courtReports[0].courtReportType?.description).isEqualTo("court report")
        Assertions.assertThat(detailResponse.convictions[0].courtReports[0].courtReportType?.code).isEqualTo("CR1")
        Assertions.assertThat(detailResponse.convictions[0].courtReports[0].author?.forenames).isEqualTo("Bob Micheal")
        Assertions.assertThat(detailResponse.convictions[0].courtReports[0].author?.surname).isEqualTo("Smith")
        Assertions.assertThat(detailResponse.convictions[0].courtReports[0].author?.unallocated).isEqualTo(false)
    }
}
