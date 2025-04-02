package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Person
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CaseSummaryIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `find by name`() {
        val person = PersonGenerator.CASE_SUMMARY
        mockMvc.perform(get("/case-summary?forename=${person.forename.uppercase()}&surname=${person.surname.lowercase()}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.page.totalElements", equalTo(1)))
            .andExpect(jsonPath("$.page.totalPages", equalTo(1)))
            .andExpect(jsonPath("$.page.size", equalTo(10)))
            .andExpect(jsonPath("$.page.number", equalTo(0)))
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpectPersonalDetailsToMatch(person, prefix = "$.content[0]")
    }

    @Test
    fun `find by crn`() {
        val person = PersonGenerator.CASE_SUMMARY
        mockMvc.perform(get("/case-summary/${person.crn}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectPersonalDetailsToMatch(person, prefix = "$")
    }

    @Test
    fun `personal details are returned`() {
        val person = PersonGenerator.CASE_SUMMARY
        val manager = PersonManagerGenerator.CASE_SUMMARY
        val address = AddressGenerator.CASE_SUMMARY_MAIN_ADDRESS
        mockMvc.perform(get("/case-summary/${person.crn}/personal-details").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectPersonalDetailsToMatch(person)
            .andExpect(jsonPath("$.communityManager.staffCode", equalTo(manager.staff.code)))
            .andExpect(jsonPath("$.mainAddress.addressNumber", equalTo(address.addressNumber)))
            .andExpect(jsonPath("$.mainAddress.streetName", equalTo(address.streetName)))
            .andExpect(jsonPath("$.mainAddress.noFixedAbode", equalTo(address.noFixedAbode)))
    }

    @Test
    fun `overview is returned`() {
        val person = PersonGenerator.CASE_SUMMARY
        val event = EventGenerator.CASE_SUMMARY
        mockMvc.perform(get("/case-summary/${person.crn}/overview").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectPersonalDetailsToMatch(person)
            .andExpect(jsonPath("$.registerFlags", equalTo(listOf("MAPPA 1", "High RoSH"))))
            .andExpect(jsonPath("$.activeConvictions[0].number", equalTo(event.number)))
            .andExpect(
                jsonPath(
                    "$.activeConvictions[0].mainOffence.description",
                    equalTo(event.mainOffence.offence.description)
                )
            )
            .andExpect(
                jsonPath(
                    "$.activeConvictions[0].additionalOffences[0].description",
                    equalTo(event.additionalOffences[0].offence.description)
                )
            )
            .andExpect(jsonPath("$.activeConvictions[0].sentence.isCustodial", equalTo(true)))
            .andExpect(
                jsonPath(
                    "$.activeConvictions[0].sentence.custodialStatusCode",
                    equalTo(event.disposal!!.custody!!.status.code)
                )
            )
            .andExpect(jsonPath("$.activeConvictions[0].sentence.sentenceExpiryDate", equalTo("2023-01-01")))
    }

    @Test
    fun `mappa and rosh history is returned`() {
        val person = PersonGenerator.CASE_SUMMARY
        mockMvc.perform(get("/case-summary/${person.crn}/mappa-and-rosh-history").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectPersonalDetailsToMatch(person)
            .andExpect(jsonPath("$.mappa.category", equalTo(1)))
            .andExpect(jsonPath("$.roshHistory[0].type", equalTo("RHRH")))
            .andExpect(jsonPath("$.roshHistory[0].typeDescription", equalTo("High RoSH")))
    }

    @Test
    fun `licence conditions are returned`() {
        val person = PersonGenerator.CASE_SUMMARY
        val event = EventGenerator.CASE_SUMMARY
        mockMvc.perform(get("/case-summary/${person.crn}/licence-conditions").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectPersonalDetailsToMatch(person)
            .andExpect(jsonPath("$.activeConvictions[0].licenceConditions.size()", equalTo(1)))
            .andExpect(jsonPath("$.activeConvictions[0].licenceConditions[0].startDate", equalTo("2020-01-01")))
            .andExpect(
                jsonPath(
                    "$.activeConvictions[0].licenceConditions[0].mainCategory.description",
                    equalTo(event.disposal!!.licenceConditions[0].mainCategory.description)
                )
            )
            .andExpect(
                jsonPath(
                    "$.activeConvictions[0].licenceConditions[0].notes",
                    equalTo(event.disposal!!.licenceConditions[0].notes)
                )
            )
    }

    @Test
    fun `contact history is returned`() {
        val person = PersonGenerator.CASE_SUMMARY
        mockMvc.perform(get("/case-summary/${person.crn}/contact-history").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectPersonalDetailsToMatch(person)
            .andExpect(
                jsonPath(
                    "$.contacts.*.notes",
                    equalTo(listOf("default", "system-generated", "documents", "AP Residence Plan Prepared", "past"))
                )
            )
            .andExpect(jsonPath("$.contacts[0].outcome", equalTo(ContactGenerator.DEFAULT_OUTCOME.description)))
            .andExpect(jsonPath("$.contacts[1].type.code", equalTo(ContactGenerator.SYSTEM_GENERATED_TYPE.code)))
            .andExpect(jsonPath("$.contacts[2].documents[*].name", equalTo(listOf("doc1", "doc2"))))
            .andExpect(jsonPath("$.contacts[4].startDateTime", equalTo("2022-01-01T12:00:00Z")))
            .andExpect(jsonPath("$.summary.hits", equalTo(5)))
            .andExpect(jsonPath("$.summary.total", equalTo(5)))
            .andExpect(jsonPath("$.summary.types.size()", equalTo(3)))
            .andExpect(jsonPath("$.summary.types[0].description", equalTo("AP Residence Plan Prepared")))
            .andExpect(jsonPath("$.summary.types[0].total", equalTo(1)))
            .andExpect(jsonPath("$.summary.types[1].description", equalTo("System-generated contact type")))
            .andExpect(jsonPath("$.summary.types[2].total", equalTo(3)))
    }

    @Test
    fun `contacts can be filtered on date`() {
        val person = PersonGenerator.CASE_SUMMARY
        mockMvc.perform(get("/case-summary/${person.crn}/contact-history?from=2022-01-01&to=2022-01-01").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.contacts.*.notes", equalTo(listOf("past"))))
            .andExpect(jsonPath("$.summary.hits", equalTo(1)))
            .andExpect(jsonPath("$.summary.total", equalTo(5)))
    }

    @Test
    fun `contacts can be excluded if they are system-generated`() {
        val person = PersonGenerator.CASE_SUMMARY
        mockMvc.perform(get("/case-summary/${person.crn}/contact-history?includeSystemGenerated=false").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.contacts.*.notes", not(hasItem("system-generated"))))
    }

    @Test
    fun `contacts can be queried on notes`() {
        val person = PersonGenerator.CASE_SUMMARY
        mockMvc.perform(get("/case-summary/${person.crn}/contact-history?query=Doc").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.contacts.*.notes", hasItem("documents")))
    }

    @Test
    fun `recommendation model is returned`() {
        val person = PersonGenerator.CASE_SUMMARY
        mockMvc.perform(get("/case-summary/${person.crn}/recommendation-model").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectPersonalDetailsToMatch(person)
            .andExpect(jsonPath("$.mainAddress.addressNumber", equalTo("123")))
            .andExpect(jsonPath("$.mainAddress.streetName", equalTo("Fake Street")))
            .andExpect(jsonPath("$.mappa.level", equalTo(2)))
            .andExpect(jsonPath("$.activeConvictions.size()", equalTo(1)))
            .andExpect(jsonPath("$.activeCustodialConvictions.size()", equalTo(1)))
            .andExpect(jsonPath("$.activeCustodialConvictions[0].sentence.startDate", equalTo("2021-01-01")))
    }

    private fun ResultActions.andExpectPersonalDetailsToMatch(
        person: Person,
        prefix: String = "$.personalDetails"
    ) = this
        .andExpect(jsonPath("$prefix.name.forename", equalTo(person.forename)))
        .andExpect(jsonPath("$prefix.name.middleName", equalTo("${person.secondName} ${person.thirdName}")))
        .andExpect(jsonPath("$prefix.name.surname", equalTo(person.surname)))
        .andExpect(jsonPath("$prefix.gender", equalTo(person.gender.description)))
        .andExpect(jsonPath("$prefix.dateOfBirth", equalTo(person.dateOfBirth.toString())))
        .andExpect(jsonPath("$prefix.identifiers.nomsNumber", equalTo(person.nomsNumber)))
        .andExpect(jsonPath("$prefix.identifiers.croNumber", equalTo(person.croNumber)))
        .andExpect(jsonPath("$prefix.identifiers.pncNumber", equalTo(person.pncNumber)))
        .andExpect(jsonPath("$prefix.identifiers.bookingNumber", equalTo(person.mostRecentPrisonerNumber)))
        .andExpect(jsonPath("$prefix.ethnicity", equalTo(person.ethnicity!!.description)))
        .andExpect(jsonPath("$prefix.primaryLanguage", equalTo(person.primaryLanguage!!.description)))
}
