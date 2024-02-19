package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.CaseIdentifiers
import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.api.model.MappaDetail
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CaseDetailIntegrationTests {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `crn is correctly returned when noms id present in delius`() {
        val identifiers = mockMvc.perform(get("/probation-cases/${PersonGenerator.DEFAULT.noms}/crn").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<CaseIdentifiers>()

        assertThat(identifiers.crn, equalTo(PersonGenerator.DEFAULT.crn))
    }

    @Test
    fun `not found returned when noms id not present in delius`() {
        mockMvc.perform(get("/probation-cases/N4567FD/crn").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `mappa detail is returned when available`() {
        val mappa = mockMvc.perform(get("/probation-cases/${PersonGenerator.DEFAULT.crn}/mappa").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<MappaDetail>()

        assertThat(mappa.category, equalTo(1))
        assertThat(mappa.level, equalTo(2))
        assertThat(mappa.startDate, equalTo(LocalDate.now().minusDays(30)))
        assertThat(mappa.reviewDate, equalTo(LocalDate.now().plusDays(60)))
    }

    @Test
    fun `community manager is returned`() {
        val manager = mockMvc
            .perform(get("/probation-cases/${PersonGenerator.DEFAULT.crn}/community-manager").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<Manager>()

        assertThat(
            manager.name,
            equalTo(Name(ProviderGenerator.DEFAULT_STAFF.forename, ProviderGenerator.DEFAULT_STAFF.surname))
        )
        assertFalse(manager.unallocated)
    }
}
