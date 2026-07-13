package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT_PERSON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PERSON_IN_PRISON
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PERSON_WITH_RESPONSIBLE_OFFICER_WITHOUT_USER
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.model.OfficeAddress
import uk.gov.justice.digital.hmpps.model.SignAndSendResponse
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.jsonPath
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SignAndSendIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `sign and send details returned with valid crn (om)`() {
        val crn = DEFAULT_PERSON.crn
        val username = UserGenerator.DEFAULT_PROBATION_USER.username
        val result = mockMvc.get("/sign-and-send/$crn/$username") { withToken() }.andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<SignAndSendResponse>()
        assertThat(result.userDetails?.forenames).isEqualTo("John Test")
        assertThat(result.userDetails?.surname).isEqualTo("Smith")
        assertThat(result.responsibleOfficer.title).isEqualTo("Mr")
        assertThat(result.responsibleOfficer.name?.forename).isEqualTo("John")
        assertThat(result.responsibleOfficer.name?.middleName).isEqualTo("Bob")
        assertThat(result.responsibleOfficer.name?.surname).isEqualTo("Smith")
        assertThat(result.responsibleOfficer.telephoneNumber).isEqualTo("07247764536")
        assertThat(result.responsibleOfficer.emailAddress).isEqualTo("john.smith@moj.gov.uk")
        assertThat(result.responsibleOfficer.addresses.size).isEqualTo(2)
        assertThat(result.responsibleOfficer.addresses[0].status).isEqualTo("Default")
        assertThat(result.responsibleOfficer.addresses[1].status).isNull()
    }

    @Test
    fun `sign and send details returned with valid crn (pom)`() {
        val crn = PERSON_IN_PRISON.crn
        val username = UserGenerator.POM_USER.username
        val result = mockMvc.get("/sign-and-send/$crn/$username") { withToken() }.andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<SignAndSendResponse>()
        assertThat(result.userDetails?.forenames).isEqualTo("Jack Test")
        assertThat(result.userDetails?.surname).isEqualTo("Harry")
        assertThat(result.responsibleOfficer.title).isEqualTo("Mr")
        assertThat(result.responsibleOfficer.name?.forename).isEqualTo("Jack")
        assertThat(result.responsibleOfficer.name?.middleName).isEqualTo("Pom")
        assertThat(result.responsibleOfficer.name?.surname).isEqualTo("Harry")
        assertThat(result.responsibleOfficer.telephoneNumber).isEqualTo("07123456789")
        assertThat(result.responsibleOfficer.emailAddress).isEqualTo("jack.harry@moj.gov.uk")
        assertThat(result.responsibleOfficer.addresses.size).isEqualTo(2)
        assertThat(result.responsibleOfficer.addresses[0].status).isEqualTo("Default")
        assertThat(result.responsibleOfficer.addresses[1].status).isNull()
    }

    @Test
    fun `username not found returns 404 response`() {
        mockMvc.get("/sign-and-send/${DEFAULT_PERSON.crn}/nonexistent") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `no home area returns no addresses`() {
        mockMvc.get("/sign-and-send/${PERSON_WITH_RESPONSIBLE_OFFICER_WITHOUT_USER.crn}/NoHomeArea") { withToken() }
            .andExpect {
                status { isOk() }
                content { jsonPath("$.responsibleOfficer.addresses", emptyList<OfficeAddress>()) }
            }
    }

    @Test
    fun `404 when crn not found`() {
        mockMvc.get("/sign-and-send/X987654") { withToken() }
            .andExpect { status { isNotFound() } }
    }
}
