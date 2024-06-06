package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.LimitedAccess
import uk.gov.justice.digital.hmpps.api.model.LimitedAccessUser
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integration.delius.entity.Alias
import uk.gov.justice.digital.hmpps.integration.delius.entity.PersonAddress
import uk.gov.justice.digital.hmpps.service.asAddress
import uk.gov.justice.digital.hmpps.service.asModel
import uk.gov.justice.digital.hmpps.service.detail
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class CorePersonIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var telemetryService: TelemetryService

    val minPerson = PersonGenerator.MIN_PERSON.detail(listOf(), listOf())
    val fullPerson = PersonGenerator.FULL_PERSON.detail(
        aliases = PersonGenerator.FULL_PERSON_ALIASES.map(Alias::asModel),
        addresses = PersonGenerator.FULL_PERSON_ADDRESSES.mapNotNull(PersonAddress::asAddress),
        exclusions = LimitedAccess(
            message = "This case is excluded because ...",
            users = listOf(LimitedAccessUser("SomeUser1"))
        ),
        restrictions = LimitedAccess(
            message = "This case is restricted because ...",
            users = listOf(LimitedAccessUser("SomeUser2"), LimitedAccessUser("FutureEndDatedUser"))
        ),
    )

    @Test
    fun `correctly returns detail by crn`() {
        mockMvc
            .perform(get("/probation-cases/${PersonGenerator.MIN_PERSON.crn}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(minPerson)
    }

    @Test
    fun `correctly returns detail by id`() {
        mockMvc
            .perform(get("/probation-cases/${PersonGenerator.FULL_PERSON.id}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpectJson(fullPerson)
    }

    @Test
    fun `correctly returns all cases`() {
        mockMvc
            .perform(get("/all-probation-cases?sort=crn,desc").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("totalElements", equalTo(2)))
            .andExpect(jsonPath("content[0].identifiers.crn", equalTo(minPerson.identifiers.crn)))
            .andExpect(jsonPath("content[1].identifiers.crn", equalTo(fullPerson.identifiers.crn)))
    }
}
