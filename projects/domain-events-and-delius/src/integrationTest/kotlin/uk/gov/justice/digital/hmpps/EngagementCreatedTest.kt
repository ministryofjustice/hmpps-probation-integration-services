package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.service.Engagement
import uk.gov.justice.digital.hmpps.service.Identifiers
import uk.gov.justice.digital.hmpps.service.Name
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class EngagementCreatedTest {
    @Autowired
    internal lateinit var mockMvc: MockMvc

    @Test
    fun `engagement details returned from detail url`() {
        val person = PersonGenerator.ENGAGEMENT_CREATED
        val engagement = mockMvc
            .perform(get("/probation-case.engagement.created/${person.crn}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<Engagement>()

        assertThat(
            engagement,
            equalTo(
                Engagement(
                    Identifiers(person.crn, person.pnc),
                    Name(person.forename, person.surname, listOf()),
                    person.dateOfBirth
                )
            )
        )
    }
}
