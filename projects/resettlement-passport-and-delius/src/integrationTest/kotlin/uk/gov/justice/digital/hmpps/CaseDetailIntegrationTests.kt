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
import uk.gov.justice.digital.hmpps.api.model.*
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

    @Test
    fun `person detail is returned correctly`() {
        val detail = mockMvc.perform(get("/probation-cases/${PersonGenerator.DEFAULT.crn}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<PersonDetail>()

        assertThat(detail.crn, equalTo(PersonGenerator.DEFAULT.crn))
        assertThat(detail.name, equalTo(Name("Fred", "Williams")))
        assertThat(detail.dateOfBirth, equalTo(LocalDate.of(1982, 8, 19)))
        assertThat(
            detail.contactDetails, equalTo(
                ContactDetails(
                    "020 346 7982",
                    "07452819463",
                    "freddy@justice.co.uk"
                )
            )
        )
    }
}
