package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.AddressGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CaseSummaryIntegrationTest {
    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var wireMockserver: WireMockServer

    @Test
    fun `personal details are returned`() {
        val person = PersonGenerator.CASE_SUMMARY
        val address = AddressGenerator.CASE_SUMMARY_MAIN_ADDRESS
        mockMvc.perform(get("/case-summary/${person.crn}/personal-details").withOAuth2Token(wireMockserver))
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.name.forename", equalTo(person.forename)))
            .andExpect(jsonPath("$.name.middleName", equalTo("${person.secondName} ${person.thirdName}")))
            .andExpect(jsonPath("$.name.surname", equalTo(person.surname)))
            .andExpect(jsonPath("$.gender", equalTo(person.gender.description)))
            .andExpect(jsonPath("$.dateOfBirth", equalTo(person.dateOfBirth.toString())))
            .andExpect(jsonPath("$.identifiers.nomsNumber", equalTo(person.nomsNumber)))
            .andExpect(jsonPath("$.identifiers.croNumber", equalTo(person.croNumber)))
            .andExpect(jsonPath("$.identifiers.pncNumber", equalTo(person.pncNumber)))
            .andExpect(jsonPath("$.identifiers.bookingNumber", equalTo(person.mostRecentPrisonerNumber)))
            .andExpect(jsonPath("$.ethnicity", equalTo(person.ethnicity!!.description)))
            .andExpect(jsonPath("$.primaryLanguage", equalTo(person.primaryLanguage!!.description)))
            .andExpect(jsonPath("$.mainAddress.addressNumber", equalTo(address.addressNumber)))
            .andExpect(jsonPath("$.mainAddress.streetName", equalTo(address.streetName)))
            .andExpect(jsonPath("$.mainAddress.noFixedAbode", equalTo(address.noFixedAbode)))
    }

    @Test
    fun `overview is returned`() {
        val person = PersonGenerator.CASE_SUMMARY
        val manager = PersonManagerGenerator.CASE_SUMMARY
        val event = EventGenerator.CASE_SUMMARY
        val address = AddressGenerator.CASE_SUMMARY_MAIN_ADDRESS
        mockMvc.perform(get("/case-summary/${person.crn}/overview").withOAuth2Token(wireMockserver))
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.personalDetails.name.surname", equalTo(person.surname)))
            .andExpect(jsonPath("$.personalDetails.mainAddress.streetName", equalTo(address.streetName)))
            .andExpect(jsonPath("$.communityManager.staffCode", equalTo(manager.staff.code)))
            .andExpect(jsonPath("$.registerFlags", equalTo(listOf("MAPPA 1", "High RoSH"))))
            .andExpect(jsonPath("$.activeConvictions[0].number", equalTo(event.number)))
            .andExpect(jsonPath("$.activeConvictions[0].mainOffence", equalTo(event.mainOffence.offence.description)))
            .andExpect(jsonPath("$.activeConvictions[0].additionalOffences[0]", equalTo(event.additionalOffences[0].offence.description)))
            .andExpect(jsonPath("$.activeConvictions[0].sentence.isCustodial", equalTo(true)))
            .andExpect(jsonPath("$.activeConvictions[0].sentence.custodialStatusCode", equalTo(event.disposal!!.custody!!.status.code)))
            .andExpect(jsonPath("$.activeConvictions[0].sentence.sentenceExpiryDate", equalTo("2023-01-01")))
    }
}
