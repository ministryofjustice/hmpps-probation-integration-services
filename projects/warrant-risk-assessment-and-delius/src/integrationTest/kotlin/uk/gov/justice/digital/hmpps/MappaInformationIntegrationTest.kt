package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.model.MappaInformation
import uk.gov.justice.digital.hmpps.model.MappaRegistration
import uk.gov.justice.digital.hmpps.model.MappaType
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest
internal class MappaInformationIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
) {
    @Test
    fun `returns 404 when CRN does not exist`() {
        mockMvc.get("/mappa-information/NOTFOUND") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `returns MAPPA information with latest registration`() {
        val person = PersonGenerator.DEFAULT
        val response = mockMvc.get("/mappa-information/${person.crn}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<MappaInformation>()

        assertThat(response.subjectOfMappaProcedures).isTrue()
        assertThat(response.mappaRegistration).isNotNull
        assertThat(response.mappaRegistration?.id).isEqualTo(RegistrationGenerator.MAPPA_REGISTRATION.id)
        assertThat(response.mappaRegistration?.type?.code).isEqualTo("M1")
        assertThat(response.mappaRegistration?.type?.description).isEqualTo("MAPPA Level 1")
        assertThat(response.mappaRegistration?.startDate).isEqualTo(LocalDate.of(2025, 1, 1))
        assertThat(response.mappaRegistration?.notes).isEqualTo("some notes in here")
    }

    @Test
    fun `returns latest MAPPA registration when multiple exist`() {
        val person = PersonGenerator.DEFAULT
        val response = mockMvc.get("/mappa-information/${person.crn}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<MappaInformation>()

        // Should return the latest (2025-01-01), not the older one (2024-06-01)
        assertThat(response.mappaRegistration?.startDate).isEqualTo(LocalDate.of(2025, 1, 1))
        assertThat(response.mappaRegistration?.type?.code).isEqualTo("M1")
    }

    @Test
    fun `returns subjectOfMappaProcedures false when no MAPPA registration exists`() {
        val person = PersonGenerator.NO_OPTIONAL_FIELDS // Person without MAPPA
        val response = mockMvc.get("/mappa-information/${person.crn}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<MappaInformation>()

        assertThat(response.subjectOfMappaProcedures).isFalse()
        assertThat(response.mappaRegistration).isNull()
    }
}