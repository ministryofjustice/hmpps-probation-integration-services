package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageRequest
import uk.gov.justice.digital.hmpps.api.model.activity.PersonActivitySearchRequest
import uk.gov.justice.digital.hmpps.client.ActivitySearchRequest
import uk.gov.justice.digital.hmpps.client.ContactSearchResponse
import uk.gov.justice.digital.hmpps.client.ContactSearchResult
import uk.gov.justice.digital.hmpps.client.ProbationSearchClient
import uk.gov.justice.digital.hmpps.client.ProbationSearchPreloadClient
import uk.gov.justice.digital.hmpps.client.ProbationSearchSemanticClient
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator.PERSONAL_DETAILS
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.utils.Summary
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class ActivityServiceTest {

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var probationSearchClient: ProbationSearchClient

    @Mock
    lateinit var probationSearchSemanticClient: ProbationSearchSemanticClient

    @Mock
    lateinit var probationSearchPreloadClient: ProbationSearchPreloadClient

    @InjectMocks
    lateinit var service: ActivityService

    private lateinit var personSummary: Summary

    @BeforeEach
    fun setup() {
        personSummary = Summary(
            id = PERSONAL_DETAILS.id,
            forename = PERSONAL_DETAILS.forename,
            secondName = PERSONAL_DETAILS.secondName,
            surname = PERSONAL_DETAILS.surname, crn = PERSONAL_DETAILS.crn, pnc = PERSONAL_DETAILS.pnc,
            noms = PERSONAL_DETAILS.noms, dateOfBirth = PERSONAL_DETAILS.dateOfBirth.atStartOfDay()
        )
    }

    @Test
    fun `calls get person activity function returns past and future activities in a single list sorted in date descending order`() {
        val crn = "X000005"
        val contacts = listOf(
            ContactGenerator.PREVIOUS_APPT_CONTACT,
            ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT,
            ContactGenerator.NEXT_APPT_CONTACT,
            ContactGenerator.FIRST_APPT_CONTACT,
            ContactGenerator.FIRST_NON_APPT_CONTACT
        )
        whenever(personRepository.findSummary(crn)).thenReturn(personSummary)
        whenever(contactRepository.findByPersonId(any())).thenReturn(contacts)
        val res = service.getPersonActivity(crn)
        assertThat(
            res.personSummary, equalTo(PERSONAL_DETAILS.toSummary())
        )
        assertThat(
            res.activities, equalTo(
                listOf(
                    ContactGenerator.NEXT_APPT_CONTACT,
                    ContactGenerator.FIRST_APPT_CONTACT,
                    ContactGenerator.FIRST_NON_APPT_CONTACT,
                    ContactGenerator.PREVIOUS_APPT_CONTACT,
                    ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT
                ).map { it.toActivity() }
            ))
    }

    @Test
    fun `activitySearch passes filter fields through to probation search client`() {
        val crn = "X000005"
        val searchRequest = PersonActivitySearchRequest(
            keywords = "test",
            dateFrom = LocalDate.of(2024, 1, 1),
            dateTo = LocalDate.of(2024, 6, 30),
            includeSystemGenerated = false,
            filterBySparksContacts = true,
            filterBySupervisionPackageContacts = true,
            filters = listOf("COMPLIED"),
            typeCodes = listOf("APAT")
        )
        val pageable = PageRequest.of(0, 10)
        val contact = ContactGenerator.FIRST_APPT_CONTACT

        whenever(personRepository.findSummary(crn)).thenReturn(personSummary)
        whenever(probationSearchClient.contactSearch(any(), eq(0), eq(10))).thenReturn(
            ContactSearchResponse(
                size = 10,
                page = 0,
                totalResults = 1,
                totalPages = 1,
                results = listOf(ContactSearchResult(crn = crn, id = contact.id))
            )
        )
        whenever(contactRepository.findByPersonIdAndIdIn(any(), any())).thenReturn(listOf(contact))

        val res = service.activitySearch(crn, "1", searchRequest, pageable)

        val captor = argumentCaptor<ActivitySearchRequest>()
        verify(probationSearchClient).contactSearch(captor.capture(), eq(0), eq(10))
        val captured = captor.firstValue

        assertThat(captured.crn, equalTo(crn))
        assertThat(captured.keywords, equalTo("test"))
        assertThat(captured.dateFrom, equalTo(LocalDate.of(2024, 1, 1)))
        assertThat(captured.dateTo, equalTo(LocalDate.of(2024, 6, 30)))
        assertThat(captured.includeSystemGenerated, equalTo(false))
        assertThat(captured.filterBySparksContacts, equalTo(true))
        assertThat(captured.filterBySupervisionPackageContacts, equalTo(true))
        assertThat(captured.filters, equalTo(listOf("COMPLIED")))
        assertThat(captured.typeCodes, equalTo(listOf("APAT")))

        assertThat(res.totalResults, equalTo(1L))
        assertThat(res.activities.size, equalTo(1))
        assertThat(res.activities[0], equalTo(contact.toActivity()))
    }
}