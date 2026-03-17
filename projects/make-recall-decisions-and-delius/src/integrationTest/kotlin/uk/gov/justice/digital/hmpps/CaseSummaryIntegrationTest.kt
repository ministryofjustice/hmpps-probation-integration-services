package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Person
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CaseSummaryIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `find by name`() {
        val person = PersonGenerator.CASE_SUMMARY
        mockMvc.post("/case-summary/search") {
            withToken()
            json = Name(forename = person.forename.uppercase(), surname = person.surname.lowercase())
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.page.totalElements") { value(1) }
                jsonPath("$.page.totalPages") { value(1) }
                jsonPath("$.page.size") { value(10) }
                jsonPath("$.page.number") { value(0) }
                jsonPath("$.content") { value(hasSize<Int>(1)) }
            }
            .andExpectPersonalDetailsToMatch(person, prefix = "$.content[0]")
    }

    @Test
    fun `find by name accepts paging parameters`() {
        val person = PersonGenerator.CASE_SUMMARY
        mockMvc.post("/case-summary/search") {
            withToken()
            param("page", "2")
            param("size", "100")
            json = Name(forename = person.forename.uppercase(), surname = person.surname.lowercase())
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.page.totalElements") { value(1) }
                jsonPath("$.page.totalPages") { value(1) }
                jsonPath("$.page.size") { value(100) }
                jsonPath("$.page.number") { value(2) }
                jsonPath("$.content") { value(hasSize<Int>(0)) }
            }
    }

    @Test
    fun `find by crn`() {
        val person = PersonGenerator.CASE_SUMMARY
        mockMvc.get("/case-summary/${person.crn}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andExpectPersonalDetailsToMatch(person, prefix = "$")
    }

    @Test
    fun `personal details are returned`() {
        val person = PersonGenerator.CASE_SUMMARY
        val manager = PersonManagerGenerator.CASE_SUMMARY
        val address = AddressGenerator.CASE_SUMMARY_MAIN_ADDRESS
        mockMvc.get("/case-summary/${person.crn}/personal-details") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.communityManager.staffCode") { value(manager.staff.code) }
                jsonPath("$.mainAddress.addressNumber") { value(address.addressNumber) }
                jsonPath("$.mainAddress.streetName") { value(address.streetName) }
                jsonPath("$.mainAddress.noFixedAbode") { value(address.noFixedAbode) }
            }
            .andExpectPersonalDetailsToMatch(person)
    }

    @Test
    fun `overview is returned`() {
        val person = PersonGenerator.CASE_SUMMARY
        val event = EventGenerator.CASE_SUMMARY
        mockMvc.get("/case-summary/${person.crn}/overview") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.registerFlags") { value(arrayListOf("MAPPA 1", "High RoSH")) }
                jsonPath("$.activeConvictions[0].number") { value(event.number) }
                jsonPath("$.activeConvictions[0].mainOffence.description") { value(event.mainOffence.offence.description) }
                jsonPath("$.activeConvictions[0].additionalOffences[0].description") { value(event.additionalOffences[0].offence.description) }
                jsonPath("$.activeConvictions[0].sentence.isCustodial") { value(true) }
                jsonPath("$.activeConvictions[0].sentence.custodialStatusCode") { value(event.disposal!!.custody!!.status.code) }
                jsonPath("$.activeConvictions[0].sentence.sentenceExpiryDate") { value("2023-01-01") }
            }
            .andExpectPersonalDetailsToMatch(person)
    }

    @Test
    fun `mappa and rosh history is returned`() {
        val person = PersonGenerator.CASE_SUMMARY
        mockMvc.get("/case-summary/${person.crn}/mappa-and-rosh-history") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.mappa.category") { value(1) }
                jsonPath("$.roshHistory[0].type") { value("RHRH") }
                jsonPath("$.roshHistory[0].typeDescription") { value("High RoSH") }
            }
            .andExpectPersonalDetailsToMatch(person)
    }

    @Test
    fun `licence conditions are returned`() {
        val person = PersonGenerator.CASE_SUMMARY
        val event = EventGenerator.CASE_SUMMARY
        mockMvc.get("/case-summary/${person.crn}/licence-conditions") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.activeConvictions[0].licenceConditions.size()") { value(1) }
                jsonPath("$.activeConvictions[0].licenceConditions[0].startDate") { value("2020-01-01") }
                jsonPath("$.activeConvictions[0].licenceConditions[0].mainCategory.description") {
                    value(event.disposal!!.licenceConditions[0].mainCategory.description)
                }
                jsonPath("$.activeConvictions[0].licenceConditions[0].notes") {
                    value(event.disposal!!.licenceConditions[0].notes)
                }
            }
            .andExpectPersonalDetailsToMatch(person)
    }

    @Test
    fun `contact history is returned`() {
        val person = PersonGenerator.CASE_SUMMARY
        mockMvc.get("/case-summary/${person.crn}/contact-history") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.contacts.*.notes") {
                    value(
                        equalTo(
                            listOf(
                                "default",
                                "system-generated",
                                "documents",
                                "AP Residence Plan Prepared",
                                "past"
                            )
                        )
                    )
                }
                jsonPath("$.contacts[0].outcome") { value(ContactGenerator.DEFAULT_OUTCOME.description) }
                jsonPath("$.contacts[1].type.code") { value(ContactGenerator.SYSTEM_GENERATED_TYPE.code) }
                jsonPath("$.contacts[2].documents[*].name") { value(arrayListOf("doc1", "doc2")) }
                jsonPath("$.contacts[4].startDateTime") { value("2022-01-01T12:00:00Z") }
                jsonPath("$.summary.hits") { value(5) }
                jsonPath("$.summary.total") { value(5) }
                jsonPath("$.summary.types.size()") { value(3) }
                jsonPath("$.summary.types[0].description") { value("AP Residence Plan Prepared") }
                jsonPath("$.summary.types[0].total") { value(1) }
                jsonPath("$.summary.types[1].description") { value("System-generated contact type") }
                jsonPath("$.summary.types[2].total") { value(3) }
            }
            .andExpectPersonalDetailsToMatch(person)
    }

    @Test
    fun `contacts can be filtered on date`() {
        val person = PersonGenerator.CASE_SUMMARY
        mockMvc.get("/case-summary/${person.crn}/contact-history?from=2022-01-01&to=2022-01-01") {
            withToken()
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.contacts.*.notes") { value(arrayListOf("past")) }
                jsonPath("$.summary.hits") { value(1) }
                jsonPath("$.summary.total") { value(5) }
            }
    }

    @Test
    fun `contacts can be excluded if they are system-generated`() {
        val person = PersonGenerator.CASE_SUMMARY
        mockMvc.get("/case-summary/${person.crn}/contact-history?includeSystemGenerated=false") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.contacts.*.notes") { value(not(hasItem("system-generated"))) }
            }
    }

    @Test
    fun `contacts can be queried on notes`() {
        val person = PersonGenerator.CASE_SUMMARY
        mockMvc.get("/case-summary/${person.crn}/contact-history?query=Doc") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.contacts.*.notes") { value(hasItem("documents")) }
            }
    }

    @Test
    fun `recommendation model is returned`() {
        val person = PersonGenerator.CASE_SUMMARY
        mockMvc.get("/case-summary/${person.crn}/recommendation-model") {
            withToken()
        }.andExpect {
            status { is2xxSuccessful() }
            jsonPath("$.mainAddress.addressNumber") { value("123") }
            jsonPath("$.mainAddress.streetName") { value("Fake Street") }
            jsonPath("$.mappa.level") { value(2) }
            jsonPath("$.activeConvictions.size()") { value(1) }
            jsonPath("$.activeCustodialConvictions.size()") { value(1) }
            jsonPath("$.activeCustodialConvictions[0].sentence.startDate") { value("2021-01-01") }
        }.andExpectPersonalDetailsToMatch(person)
    }

    private fun ResultActionsDsl.andExpectPersonalDetailsToMatch(
        person: Person,
        prefix: String = "$.personalDetails"
    ) = this.andExpect {
        jsonPath("$prefix.name.forename") { value(person.forename) }
        jsonPath("$prefix.name.middleName") { value("${person.secondName} ${person.thirdName}") }
        jsonPath("$prefix.name.surname") { value(person.surname) }
        jsonPath("$prefix.gender") { value(person.gender.description) }
        jsonPath("$prefix.dateOfBirth") { value(person.dateOfBirth.toString()) }

        jsonPath("$prefix.identifiers.nomsNumber") { value(person.nomsNumber) }
        jsonPath("$prefix.identifiers.croNumber") { value(person.croNumber) }
        jsonPath("$prefix.identifiers.pncNumber") { value(person.pncNumber) }
        jsonPath("$prefix.identifiers.bookingNumber") { value(person.mostRecentPrisonerNumber) }

        jsonPath("$prefix.ethnicity") { value(person.ethnicity!!.description) }
        jsonPath("$prefix.primaryLanguage") { value(person.primaryLanguage!!.description) }
    }
}

