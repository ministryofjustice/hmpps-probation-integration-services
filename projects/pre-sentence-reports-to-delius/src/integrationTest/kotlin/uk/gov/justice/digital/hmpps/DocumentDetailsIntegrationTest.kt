package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.model.DefendantDetails
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest
class DocumentDetailsIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
) {
    @Test
    fun `can get details via document`() {
        val psrUuid = DocumentGenerator.DOCUMENT_UUID
        mockMvc.get("/report/$psrUuid/defendant-details") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
    }

    @Test
    fun `unknown document returns 404`() {
        val psrUuid = DocumentGenerator.DOCUMENT_INVALID_UUID
        mockMvc.get("/report/$psrUuid/defendant-details") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `assert document details are correct`() {
        val psrUuid = DocumentGenerator.DOCUMENT_UUID
        val response = mockMvc.get("/report/$psrUuid/defendant-details") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<DefendantDetails>()

        assertThat(response.crn).isEqualTo("X012771")
        assertThat(response.eventNumber).isEqualTo(1L)
        assertThat(response.name?.forename).isEqualTo("Bob")
        assertThat(response.dateOfBirth).isEqualTo(LocalDate.of(1980, 1, 1))
    }
}