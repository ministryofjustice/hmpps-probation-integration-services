package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.data.generator.CaseDetailsGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProbationCaseResourceTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @ParameterizedTest
    @MethodSource("existingCases")
    fun `retrieve responsible officer`(person: Person, communityResponsible: Boolean) {
        val staff = ProviderGenerator.JOHN_SMITH

        val ro = mockMvc
            .perform(get("/probation-case/${person.crn}/responsible-officer").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<ResponsibleOfficer>()

        val com = ro.communityManager
        assertThat(com.code, equalTo(staff.code))
        assertThat(com.name, equalTo(Name(staff.forename, staff.surname)))
        assertThat(com.username, equalTo(staff.user?.username))
        assertThat(com.email, equalTo("john.smith@moj.gov.uk"))
        assertThat(com.telephoneNumber, equalTo("07321165373"))
        assertThat(com.responsibleOfficer, equalTo(communityResponsible))
        assertThat(com.pdu.code, equalTo(ProviderGenerator.PROBATION_BOROUGH.code))
        assertThat(com.team.code, equalTo("N01PRO"))
        assertThat(com.team.email, equalTo("team@N01PRO.co.uk"))
        assertThat(com.team.telephoneNumber, equalTo("020 785 4451"))
        assertThat(com.unallocated, equalTo(false))

        if (communityResponsible) {
            assertNull(ro.prisonManager)
        } else {
            assertNotNull(ro.prisonManager)
            assertTrue(ro.prisonManager!!.responsibleOfficer)
            assertThat(ro.prisonManager!!.email, equalTo("manager@prison.gov.uk"))
            assertThat(ro.prisonManager!!.pdu.code, equalTo(ProviderGenerator.PRISON_BOROUGH.code))
        }
    }

    @Test
    fun `crn not found returns 404`() {
        mockMvc
            .perform(get("/probation-case/InvalidCrn/responsible-officer").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `nomsId returned when populated`() {
        val identifiers = mockMvc
            .perform(get("/probation-case/${PersonGenerator.DEFAULT.crn}/identifiers").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<CaseIdentifier>()

        assertThat(identifiers.nomsId, equalTo("A1234YZ"))
    }

    @Test
    fun `case details returns 404 when not found`() {
        mockMvc.perform(get("/probation-case/InvalidCrn/detail").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `basic details returned for a case successfully`() {
        val caseDetail = mockMvc
            .perform(get("/probation-case/${CaseDetailsGenerator.MINIMAL_PERSON.crn}/details").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<CaseDetail>()

        assertThat(
            caseDetail,
            equalTo(
                CaseDetail(
                    "M123456",
                    Name("Minimal", "Person"),
                    LocalDate.now().minusYears(27),
                    null,
                    null,
                    ContactDetails(false, null, null, null, null)
                )
            )
        )
    }

    @Test
    fun `full details returned for case when available`() {
        val caseDetail = mockMvc
            .perform(get("/probation-case/${CaseDetailsGenerator.FULL_PERSON.crn}/details").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<CaseDetail>()

        assertFullPersonDetails(caseDetail)
    }

    @Test
    fun `conviction details returned for case when available`() {
        val cc = mockMvc
            .perform(get("/probation-case/${CaseDetailsGenerator.FULL_PERSON.crn}/convictions").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<CaseConvictions>()

        assertFullPersonDetails(cc.caseDetail)
        assertThat(cc.convictions.size, equalTo(1))
        assertFullConvictionDetails(cc.convictions.first())
    }

    @Test
    fun `conviction details returned for individual conviction when available`() {
        val cc = mockMvc
            .perform(get("/probation-case/${CaseDetailsGenerator.FULL_PERSON.crn}/convictions/${SentenceGenerator.FULL_DETAIL_EVENT.id}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<CaseConviction>()

        assertFullPersonDetails(cc.caseDetail)
        assertFullConvictionDetails(cc.conviction)
    }

    companion object {
        @JvmStatic
        fun existingCases() = listOf(
            Arguments.of(PersonGenerator.COMMUNITY_RESPONSIBLE, true),
            Arguments.of(PersonGenerator.COMMUNITY_NOT_RESPONSIBLE, false)
        )
    }

    private fun assertFullPersonDetails(caseDetail: CaseDetail) {
        assertThat(
            caseDetail,
            equalTo(
                CaseDetail(
                    "F987462",
                    Name("Full", "Person"),
                    LocalDate.now().minusYears(42),
                    "Description of GEN1",
                    Profile(
                        "Description of LANG1",
                        "Description of ETH1",
                        "Description of REL1",
                        listOf(
                            Disability(
                                "Description of DIS1",
                                LocalDate.now().minusDays(14),
                                "Some notes about the disability"
                            )
                        )
                    ),
                    contactDetails = ContactDetails(
                        noFixedAbode = false,
                        mainAddress = Address.from(
                            buildingName = "Some Building",
                            streetName = "Some Street",
                            postcode = "SB1 1SS"
                        ),
                        emailAddress = "someone@somewhere.com",
                        telephoneNumber = "0191 234 6718",
                        mobileNumber = "07453351625"
                    )
                )
            )
        )
    }

    private fun assertFullConvictionDetails(conviction: Conviction) {
        val event = SentenceGenerator.FULL_DETAIL_EVENT
        val disposal = SentenceGenerator.FULL_DETAIL_SENTENCE
        val mainOffence = SentenceGenerator.FULL_DETAIL_MAIN_OFFENCE
        assertThat(
            conviction,
            equalTo(
                Conviction(
                    event.id,
                    event.convictionDate!!,
                    Sentence(disposal.type.description, disposal.expectedEndDate()),
                    Offence(mainOffence.offence.mainCategoryDescription, mainOffence.offence.subCategoryDescription),
                    true
                )
            )
        )
    }
}
