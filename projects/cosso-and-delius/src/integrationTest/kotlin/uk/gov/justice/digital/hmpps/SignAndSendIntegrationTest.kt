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
import uk.gov.justice.digital.hmpps.model.SignAndSendResponse
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SignAndSendIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `sign and send details returned with valid crn (om)`() {
        val crn = DEFAULT_PERSON.crn
        val result = mockMvc.get("/sign-and-send/$crn") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<SignAndSendResponse>()
        assertThat(result.userDetails?.forename).isEqualTo("John")
        assertThat(result.userDetails?.surname).isEqualTo("Smith")
        assertThat(result.responsibleOfficer?.title).isEqualTo("Mr")
        assertThat(result.responsibleOfficer?.name?.forename).isEqualTo("John")
        assertThat(result.responsibleOfficer?.name?.middleName).isEqualTo("Bob")
        assertThat(result.responsibleOfficer?.name?.surname).isEqualTo("Smith")
        assertThat(result.telephoneNumber).isEqualTo("07247764536")
        assertThat(result.emailAddress).isEqualTo("john.smith@moj.gov.uk")
        assertThat(result.addresses.size).isEqualTo(2)
        assertThat(result.addresses[0].status).isEqualTo("Default")
        assertThat(result.addresses[1].status).isNull()
    }

    @Test
    fun `sign and send details returned with valid crn (pom)`() {
        val crn = PERSON_IN_PRISON.crn
        val result = mockMvc.get("/sign-and-send/$crn") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<SignAndSendResponse>()
        assertThat(result.userDetails?.forename).isEqualTo("Jack")
        assertThat(result.userDetails?.surname).isEqualTo("Harry")
        assertThat(result.responsibleOfficer?.title).isEqualTo("Mr")
        assertThat(result.responsibleOfficer?.name?.forename).isEqualTo("Jack")
        assertThat(result.responsibleOfficer?.name?.middleName).isEqualTo("Pom")
        assertThat(result.responsibleOfficer?.name?.surname).isEqualTo("Harry")
        assertThat(result.telephoneNumber).isEqualTo("07123456789")
        assertThat(result.emailAddress).isEqualTo("jack.harry@moj.gov.uk")
        assertThat(result.addresses.size).isEqualTo(2)
        assertThat(result.addresses[0].status).isEqualTo("Default")
        assertThat(result.addresses[1].status).isNull()
    }

    @Test
    fun `can get responsible officer details when user is missing`() {
        val crn = PERSON_WITH_RESPONSIBLE_OFFICER_WITHOUT_USER.crn
        val result = mockMvc.get("/sign-and-send/$crn") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<SignAndSendResponse>()
        assertThat(result.userDetails).isNull()
        assertThat(result.responsibleOfficer?.title).isNull()
        assertThat(result.responsibleOfficer?.name?.forename).isEqualTo("Jane")
        assertThat(result.responsibleOfficer?.name?.middleName).isEqualTo("Mary")
        assertThat(result.responsibleOfficer?.name?.surname).isEqualTo("Doe")
        assertThat(result.telephoneNumber).isNull()
        assertThat(result.emailAddress).isNull()
        assertThat(result.addresses.size).isEqualTo(0)
    }

    @Test
    fun `404 when crn not found`() {
        mockMvc.get("/sign-and-send/X987654") {
            withToken()
        }
            .andExpect { status { isNotFound() } }
    }
}
