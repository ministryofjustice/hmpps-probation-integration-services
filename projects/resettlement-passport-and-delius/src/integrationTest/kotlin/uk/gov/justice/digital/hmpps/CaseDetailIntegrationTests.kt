package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.CaseIdentifiers
import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.api.model.MappaDetail
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CaseDetailIntegrationTests {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `crn is correctly returned when noms id present in delius`() {
        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("/probation-cases/${PersonGenerator.DEFAULT.noms}/crn")
                .withOAuth2Token(wireMockServer)
        ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful).andReturn().response

        val identifiers = objectMapper.readValue<CaseIdentifiers>(result.contentAsString)
        assertThat(identifiers.crn, equalTo(PersonGenerator.DEFAULT.crn))
    }

    @Test
    fun `not found returned when noms id not present in delius`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/probation-cases/N4567FD/crn")
                .withOAuth2Token(wireMockServer)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `mappa detail is returned when available`() {
        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("/probation-cases/${PersonGenerator.DEFAULT.crn}/mappa")
                .withOAuth2Token(wireMockServer)
        ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful).andReturn().response

        val mappa = objectMapper.readValue<MappaDetail>(result.contentAsString)
        assertThat(mappa.category, equalTo(1))
        assertThat(mappa.level, equalTo(2))
        assertThat(mappa.startDate, equalTo(LocalDate.now().minusDays(30)))
        assertThat(mappa.reviewDate, equalTo(LocalDate.now().plusDays(60)))
    }

    @Test
    fun `community manager is returned`() {
        val result = mockMvc.perform(
            MockMvcRequestBuilders.get("/probation-cases/${PersonGenerator.DEFAULT.crn}/community-manager")
                .withOAuth2Token(wireMockServer)
        ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful).andReturn().response

        val manager = objectMapper.readValue<Manager>(result.contentAsString)
        assertThat(
            manager.name,
            equalTo(Name(ProviderGenerator.DEFAULT_STAFF.forename, ProviderGenerator.DEFAULT_STAFF.surname))
        )
    }
}
