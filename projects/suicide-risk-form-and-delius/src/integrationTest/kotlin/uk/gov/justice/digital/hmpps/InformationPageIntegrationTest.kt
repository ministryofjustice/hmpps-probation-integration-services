package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.codedDescription
import uk.gov.justice.digital.hmpps.model.InformationPageResponse
import uk.gov.justice.digital.hmpps.model.MostRecentRegistration
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest
internal class InformationPageIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val wireMockServer: WireMockServer,
    private val registrationRepository: RegistrationRepository
) {

    @Test
    fun `can retrieve information page details for suicide risk registration`() {
        val person = PersonGenerator.DEFAULT_PERSON

        val response = mockMvc.get("/information-page/${person.crn}") {
            withToken()
        }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<InformationPageResponse>()

        val registration = RegistrationGenerator.SUICIDE_SELF_HARM_REGISTRATION

        assertThat(response).isEqualTo(
            InformationPageResponse(
                registration = MostRecentRegistration(
                    id = registration.id,
                    type = registration.type.codedDescription(),
                    startDate = registration.date,
                    endDate = null,
                    notes = registration.notes,
                    documentsLinked = registration.documentLinked!!,
                    deregistered = registration.deregistered
                )
            )
        )
    }

    @Test
    fun `non-existent crn returns 404`() {
        mockMvc.get("/information-page/Z000001") {
            withToken()
        }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `crn with no registrations returns null`() {
        val response = mockMvc.get("/information-page/A000002") {
            withToken()
        }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<InformationPageResponse>()

        assertThat(response.registration).isNull()
    }

    @Test
    fun `non suicide or self harm registrations are ignored`() {
        registrationRepository.save(RegistrationGenerator.CONTACT_SUSPENDED_REGISTRATION)

        val person = PersonGenerator.DEFAULT_PERSON

        val response = mockMvc.get("/information-page/${person.crn}") {
            withToken()
        }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<InformationPageResponse>()


        assertThat(response.registration!!.type.code).isNotEqualTo("PRC")
    }
}