package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.model.FailureAndEnforcementResponse
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.util.UUID

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class FailureAndEnforcementIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @Test
    fun `can get failure and enforcement`() {
        val crn = PersonGenerator.DEFAULT_PERSON.crn
        val cossoId = DocumentGenerator.DEFAULT_DOCUMENT_UUID.toString()
        val response = mockMvc.get("/failures-enforcements/$crn/$cossoId") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<FailureAndEnforcementResponse>()
        // check that the enforceable contact is returned
        assertThat(response.enforceableContacts.size).isEqualTo(1)
        assertThat(response.enforceableContacts[0].id).isEqualTo(ContactGenerator.DEFAULT_ENFORCEABLE_CONTACT.id)
        // check that the registration flagged as ALT7 or ALSH is returned
        assertThat(response.registrations.size).isEqualTo(1)
        assertThat(response.registrations[0].type.code).isEqualTo("ALT7")
    }

    @Test
    fun `crn not found throws 404 not found`() {
        val crn = "P0981273"
        val cossoId = DocumentGenerator.DEFAULT_DOCUMENT_UUID.toString()
        mockMvc.get("/failures-enforcements/$crn/$cossoId") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `cossoId not found throws 404 not found`() {
        val crn = PersonGenerator.DEFAULT_PERSON.crn
        val cossoId = UUID.randomUUID().toString()
        mockMvc.get("/failures-enforcements/$crn/$cossoId") { withToken() }
            .andExpect { status { isNotFound() } }
    }

}