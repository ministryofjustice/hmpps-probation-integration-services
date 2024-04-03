package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.repository.AuditedInteractionRepository
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.service.ContactSearchAuditRequest
import uk.gov.justice.digital.hmpps.service.ContactSearchRequest
import uk.gov.justice.digital.hmpps.service.PageRequest
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var telemetryService: TelemetryService

    @SpyBean
    lateinit var air: AuditedInteractionRepository

    @Test
    fun `api calls successfully records audit record`() {
        val person = PersonGenerator.DEFAULT
        val query = "The quick brown fox"
        val page = 0
        val pageSize = 1
        val sort = "date"
        val direction = "asc"
        val dateTime = ZonedDateTime.now()
        mockMvc
            .perform(
                post("/probation-search/audit/contact-search")
                    .withToken()
                    .withJson(
                        ContactSearchAuditRequest(
                            ContactSearchRequest(person.crn, query, true),
                            "SearchUser",
                            PageRequest(page, pageSize, sort, direction),
                            dateTime
                        )
                    )
            ).andExpect(status().isCreated)

        val audit = argumentCaptor<AuditedInteraction>()
        verify(air, timeout(2000)).save(audit.capture())

        val saved = audit.firstValue
        assertThat(saved.userId, equalTo(UserGenerator.SEARCH_USER.id))
        assertThat(saved.parameters["crn"], equalTo(person.crn))
        assertThat(saved.parameters["offenderId"], equalTo(person.id))
        assertThat(saved.parameters["query"], equalTo(query))
        assertThat(saved.parameters["matchAllTerms"], equalTo(true))
        assertThat(saved.parameters["page"], equalTo(page))
        assertThat(saved.parameters["pageSize"], equalTo(pageSize))
        assertThat(saved.parameters["sortedBy"], equalTo(sort))
        assertThat(saved.parameters["sortDirection"], equalTo(direction))
        assertThat(saved.dateTime, isCloseTo(dateTime))
    }
}
