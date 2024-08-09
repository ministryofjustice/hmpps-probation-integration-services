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
import uk.gov.justice.digital.hmpps.api.model.Registrations
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationsGenerator.DEREG_2
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class RegistrationsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `get registrations by CRN with active only set to false`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn

        val response = mockMvc
            .perform(get("/probation-case/$crn/registrations?activeOnly=false").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<Registrations>()

        assertThat(response.registrations.size, equalTo(3))
        assertThat(response.registrations[0].active, equalTo(true))
        assertThat(response.registrations[1].active, equalTo(false))
        assertThat(response.registrations[0].endDate, equalTo(DEREG_2.deRegistrationDate))
    }

    @Test
    fun `get registrations by CRN with active only set to true`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn

        val response = mockMvc
            .perform(get("/probation-case/$crn/registrations?activeOnly=true").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<Registrations>()

        assertThat(response.registrations.size, equalTo(2))
        assertThat(response.registrations[0].active, equalTo(true))
    }
}