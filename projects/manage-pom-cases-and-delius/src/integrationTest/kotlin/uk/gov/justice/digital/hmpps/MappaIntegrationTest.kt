package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.MappaDetail
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.LEVEL_M2
import uk.gov.justice.digital.hmpps.services.ErrorResponse
import uk.gov.justice.digital.hmpps.services.MappaLevel
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class MappaIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `successful retrieval of mappa detail`() {
        val mappa = mockMvc
            .perform(get("/case-records/T123456/risks/mappa").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<MappaDetail>()
        assertThat(mappa.level, equalTo(MappaLevel.toCommunityLevel(LEVEL_M2.code)))
    }

    @Test
    fun `mappa detail not found`() {
        val mappa = mockMvc
            .perform(get("/case-records/X123456/risks/mappa").withToken())
            .andExpect(status().isNotFound)
            .andReturn().response.contentAsJson<ErrorResponse>()
        assertThat(mappa.message, equalTo("MAPPA details for offender not found"))
    }

    @Test
    fun `mappa person not found`() {
        val mappa = mockMvc
            .perform(get("/case-records/W123456/risks/mappa").withToken())
            .andExpect(status().isNotFound)
            .andReturn().response.contentAsJson<ErrorResponse>()
        assertThat(mappa.message, equalTo("Person with crn of W123456 not found"))
    }
}
