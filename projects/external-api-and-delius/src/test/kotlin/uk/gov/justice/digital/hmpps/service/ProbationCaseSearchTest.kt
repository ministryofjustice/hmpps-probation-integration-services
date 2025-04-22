package uk.gov.justice.digital.hmpps.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integration.delius.entity.Person
import uk.gov.justice.digital.hmpps.integration.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integration.probationsearch.IDs
import uk.gov.justice.digital.hmpps.integration.probationsearch.OffenderDetail
import uk.gov.justice.digital.hmpps.integration.probationsearch.ProbationSearchClient
import uk.gov.justice.digital.hmpps.model.SearchRequest
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
internal class ProbationCaseSearchTest {

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var searchClient: ProbationSearchClient

    @Mock
    lateinit var telemetryService: TelemetryService

    @InjectMocks
    lateinit var probationCaseSearch: ProbationCaseSearch

    @Test
    fun `uses search result and logs to telemetry if db results are different`() {
        whenever(searchClient.findAll(any())).thenReturn(listOf(OffenderDetail(otherIds = IDs("D123456"))))
        whenever(personRepository.findAll(any<Specification<Person>>()))
            .thenReturn(listOf(PersonGenerator.generate("A123456")))

        val result = probationCaseSearch.find(SearchRequest(firstName = "James"))
        assertThat(result.first().otherIds.crn).isEqualTo("D123456")

        verify(telemetryService).trackEvent(eq("SearchMismatch"), eq(mapOf("searchFields" to "firstName", "resultsSize" to "1 / 1")), eq(emptyMap()))
    }
}