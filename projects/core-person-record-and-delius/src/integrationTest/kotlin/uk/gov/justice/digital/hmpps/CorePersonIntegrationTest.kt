package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.PersonDetail
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integration.delius.entity.Alias
import uk.gov.justice.digital.hmpps.integration.delius.entity.PersonAddress
import uk.gov.justice.digital.hmpps.service.asAddress
import uk.gov.justice.digital.hmpps.service.asModel
import uk.gov.justice.digital.hmpps.service.detail
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class CorePersonIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `correctly returns detail by crn`() {
        val detail = mockMvc
            .perform(get("/probation-cases/${PersonGenerator.MIN_PERSON.crn}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<PersonDetail>()

        assertThat(
            detail,
            equalTo(PersonGenerator.MIN_PERSON.detail(listOf(), listOf()))
        )
    }

    @Test
    fun `correctly returns detail by id`() {
        val detail = mockMvc
            .perform(get("/probation-cases/${PersonGenerator.FULL_PERSON.id}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<PersonDetail>()

        assertThat(
            detail,
            equalTo(
                PersonGenerator.FULL_PERSON.detail(
                    PersonGenerator.FULL_PERSON_ALIASES.map(Alias::asModel),
                    PersonGenerator.FULL_PERSON_ADDRESSES.mapNotNull(PersonAddress::asAddress)
                )
            )
        )
    }
}
