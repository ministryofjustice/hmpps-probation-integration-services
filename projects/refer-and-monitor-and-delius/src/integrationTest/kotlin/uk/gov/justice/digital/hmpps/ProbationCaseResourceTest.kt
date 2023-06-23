package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.Address
import uk.gov.justice.digital.hmpps.api.model.CaseDetail
import uk.gov.justice.digital.hmpps.api.model.CaseIdentifier
import uk.gov.justice.digital.hmpps.api.model.ContactDetails
import uk.gov.justice.digital.hmpps.api.model.Disability
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.Profile
import uk.gov.justice.digital.hmpps.api.model.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.data.generator.CaseDetailsGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProbationCaseResourceTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @ParameterizedTest
    @MethodSource("existingCases")
    fun `retrieve responsible officer`(person: Person, communityResponsible: Boolean) {
        val staff = ProviderGenerator.JOHN_SMITH

        val res = mockMvc.perform(
            MockMvcRequestBuilders.get("/probation-case/${person.crn}/responsible-officer")
                .withOAuth2Token(wireMockServer)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful).andReturn().response.contentAsString

        val ro = objectMapper.readValue<ResponsibleOfficer>(res)
        val com = ro.communityManager
        assertThat(com.code, equalTo(staff.code))
        assertThat(com.name, equalTo(Name(staff.forename, staff.surname)))
        assertThat(com.username, equalTo(staff.user?.username))
        assertThat(com.email, equalTo("john.smith@moj.gov.uk"))
        assertThat(com.responsibleOfficer, equalTo(communityResponsible))
        assertThat(com.pdu.code, equalTo(ProviderGenerator.PROBATION_BOROUGH.code))

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
        mockMvc.perform(
            MockMvcRequestBuilders.get("/probation-case/InvalidCrn/responsible-officer")
                .withOAuth2Token(wireMockServer)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `nomsId returned when populated`() {
        val res = mockMvc.perform(
            MockMvcRequestBuilders.get("/probation-case/${PersonGenerator.DEFAULT.crn}/identifiers")
                .withOAuth2Token(wireMockServer)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString

        val identifiers = objectMapper.readValue<CaseIdentifier>(res)
        assertThat(identifiers.nomsId, equalTo("A1234YZ"))
    }

    @Test
    fun `case details returns 404 when not found`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/probation-case/InvalidCrn/detail")
                .withOAuth2Token(wireMockServer)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `basic details returned for a case successfully`() {
        val res = mockMvc.perform(
            MockMvcRequestBuilders.get("/probation-case/${CaseDetailsGenerator.MINIMAL_PERSON.crn}/details")
                .withOAuth2Token(wireMockServer)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString

        val caseDetail = objectMapper.readValue<CaseDetail>(res)
        assertThat(
            caseDetail,
            equalTo(
                CaseDetail(
                    "M123456",
                    Name("Minimal", "Person"),
                    null,
                    null,
                    ContactDetails(false, null, null, null, null)
                )
            )
        )
    }

    @Test
    fun `full details returned for case when available`() {
        val res = mockMvc.perform(
            MockMvcRequestBuilders.get("/probation-case/${CaseDetailsGenerator.FULL_PERSON.crn}/details")
                .withOAuth2Token(wireMockServer)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString

        val caseDetail = objectMapper.readValue<CaseDetail>(res)
        assertThat(
            caseDetail,
            equalTo(
                CaseDetail(
                    "F987462",
                    Name("Full", "Person"),
                    "Description of GEN1",
                    Profile(
                        "Description of LANG1",
                        "Description of ETH1",
                        "Description of REL1",
                        listOf(
                            Disability(
                                "Description of DIS1",
                                LocalDate.now().minusDays(14),
                                null,
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

    companion object {
        @JvmStatic
        fun existingCases() = listOf(
            Arguments.of(PersonGenerator.COMMUNITY_RESPONSIBLE, true),
            Arguments.of(PersonGenerator.COMMUNITY_NOT_RESPONSIBLE, false)
        )
    }
}
