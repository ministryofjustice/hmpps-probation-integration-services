package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.overview.PreviousOrders
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiType
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.MainOffence
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RequirementRepository
import uk.gov.justice.digital.hmpps.utils.Summary
import java.time.LocalDate
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
internal class ComplianceServiceTest {

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var eventRepository: EventRepository

    @Mock
    lateinit var requirementRepository: RequirementRepository

    @Mock
    lateinit var nsiRepository: NsiRepository

    @Mock
    lateinit var activityService: ActivityService

    @Mock
    lateinit var requirementService: RequirementService

    @Mock
    lateinit var contactRepository: ContactRepository

    @InjectMocks
    lateinit var service: ComplianceService

    private lateinit var personSummary: Summary

    @BeforeEach
    fun setup() {
        personSummary = Summary(
            id = 123,
            forename = "Jane",
            secondName = null,
            surname = "Doe",
            crn = "X000005",
            pnc = null,
            noms = null,
            dateOfBirth = LocalDate.of(1990, 1, 1).atStartOfDay()
        )
    }

    @Test
    fun `calls get compliance function`() {
        val crn = "X000005"
        val events = listOf(
            mockedEvent(1, "1", inactive = true),
            mockedEvent(2, "2", inactive = true),
            mockedEvent(3, "3", inactive = false),
            mockedEvent(4, "4", inactive = false),
        )

        whenever(personRepository.findSummary(crn)).thenReturn(personSummary)

        whenever(nsiRepository.findByPersonIdAndTypeCode(personSummary.id, "BRE")).thenReturn(
            listOf(
                breachForEvent(1),
                breachForEvent(2)
            )
        )

        whenever(nsiRepository.findByPersonIdAndTypeCode(personSummary.id, "REC")).thenReturn(emptyList())
        whenever(nsiRepository.findByPersonIdAndTypeCodeAndActiveTrue(personSummary.id, "REC")).thenReturn(null)

        whenever(activityService.getPersonSentenceActivity(any(), any(), any())).thenReturn(emptyList())

        whenever(eventRepository.findByPersonId(personSummary.id)).thenReturn(events)

        val res = service.getPersonCompliance(crn, 0)
        assertThat(res.personSummary.crn, equalTo(crn))
        assertThat(res.currentSentences.size, equalTo(2))
        assertThat(res.previousOrders.breaches, equalTo(2))
        assertThat(res.previousOrders.orders.size, equalTo(2))
    }

    @Test
    fun `calls get compliance function only active events`() {
        val crn = "X000005"
        val events = listOf(
            mockedEvent(3, "3", inactive = false),
            mockedEvent(4, "4", inactive = false),
        )

        whenever(eventRepository.findByPersonId(personSummary.id)).thenReturn(events)

        whenever(nsiRepository.findByPersonIdAndTypeCode(personSummary.id, "BRE")).thenReturn(emptyList())
        whenever(nsiRepository.findByPersonIdAndTypeCode(personSummary.id, "REC")).thenReturn(emptyList())
        whenever(nsiRepository.findByPersonIdAndTypeCodeAndActiveTrue(personSummary.id, "REC")).thenReturn(null)

        whenever(activityService.getPersonSentenceActivity(any(), any(), any())).thenReturn(emptyList())

        whenever(personRepository.findSummary(crn)).thenReturn(personSummary)

        val res = service.getPersonCompliance(crn, 0)
        assertThat(res.currentSentences.size, equalTo(2))
        assertThat(res.previousOrders, equalTo(PreviousOrders(0, 0, null, emptyList())))
    }

    @Test
    fun `filters breaches and recalls by cutoff when months provided`() {
        val crn = "X000005"
        val events = listOf(
            mockedEvent(3, "3", inactive = false),
        )

        whenever(eventRepository.findByPersonId(personSummary.id)).thenReturn(events)

        // Breach outside window (before cutoff)
        val breachOutsideWindow = Nsi(
            personId = personSummary.id,
            type = NsiType("BRE", "Breach", 1),
            eventId = 3,
            actualStartDate = LocalDate.now().minusMonths(4),
            expectedStartDate = LocalDate.now().minusMonths(4),
            id = 10,
            lastUpdated = ZonedDateTime.now(),
            active = false
        )

        // Breach inside window (after cutoff)
        val breachInsideWindow = Nsi(
            personId = personSummary.id,
            type = NsiType("BRE", "Breach", 1),
            eventId = 3,
            actualStartDate = LocalDate.now().minusMonths(1),
            expectedStartDate = LocalDate.now().minusMonths(1),
            id = 11,
            lastUpdated = ZonedDateTime.now(),
            active = false
        )

        // Breach with no start date
        val breachNullStartDate = Nsi(
            personId = personSummary.id,
            type = NsiType("BRE", "Breach", 1),
            eventId = 3,
            actualStartDate = null,
            expectedStartDate = null,
            id = 12,
            lastUpdated = ZonedDateTime.now(),
            active = false
        )

        // Recall outside window (before cutoff)
        val recallOutsideWindow = Nsi(
            personId = personSummary.id,
            type = NsiType("REC", "Recall", 2),
            eventId = 3,
            actualStartDate = LocalDate.now().minusMonths(4),
            expectedStartDate = LocalDate.now().minusMonths(4),
            id = 1,
            lastUpdated = ZonedDateTime.now(),
            active = false
        )

        // Recall inside window (after cutoff)
        val recallInsideWindow = Nsi(
            personId = personSummary.id,
            type = NsiType("REC", "Recall", 2),
            eventId = 3,
            actualStartDate = LocalDate.now().minusMonths(1),
            expectedStartDate = LocalDate.now().minusMonths(1),
            id = 2,
            lastUpdated = ZonedDateTime.now(),
            active = false
        )

        // Recall with no start date
        val recallNullStartDate = Nsi(
            personId = personSummary.id,
            type = NsiType("REC", "Recall", 2),
            eventId = 3,
            actualStartDate = null,
            expectedStartDate = null,
            id = 3,
            lastUpdated = ZonedDateTime.now(),
            active = false
        )

        whenever(nsiRepository.findByPersonIdAndTypeCode(personSummary.id, "BRE")).thenReturn(
            listOf(breachOutsideWindow, breachInsideWindow, breachNullStartDate)
        )
        whenever(nsiRepository.findByPersonIdAndTypeCode(personSummary.id, "REC")).thenReturn(
            listOf(recallOutsideWindow, recallInsideWindow, recallNullStartDate)
        )
        whenever(nsiRepository.findByPersonIdAndTypeCodeAndActiveTrue(personSummary.id, "REC")).thenReturn(null)

        whenever(activityService.getPersonSentenceActivity(any(), any(), any())).thenReturn(emptyList())

        whenever(personRepository.findSummary(crn)).thenReturn(personSummary)

        // Call with months = 2 to filter breaches and recalls
        val res = service.getPersonCompliance(crn, 2)
        assertThat(res.currentSentences.size, equalTo(1))
        // Only one breach and one recall should be in the windowed lists (the inside window ones)
    }

    private fun breachForEvent(eventId: Long) = Nsi(
        personId = personSummary.id,
        type = NsiType("BRE", "Breach", 1),
        eventId = eventId,
        actualStartDate = LocalDate.now(),
        expectedStartDate = LocalDate.now(),
        id = eventId,
        lastUpdated = ZonedDateTime.now(),
        active = false
    )

    private fun mockedEvent(id: Long, eventNumber: String, inactive: Boolean): Event {
        val offence = mock<Offence>()
        whenever(offence.code).thenReturn("M1")
        whenever(offence.description).thenReturn("Main offence")

        val mainOffence = mock<MainOffence>()
        whenever(mainOffence.offence).thenReturn(offence)

        val disposalType = mock<DisposalType>()
        whenever(disposalType.description).thenReturn("Community order")

        val disposal = mock<Disposal>()
        whenever(disposal.id).thenReturn(id)
        whenever(disposal.type).thenReturn(disposalType)
        whenever(disposal.length).thenReturn(12)
        whenever(disposal.date).thenReturn(LocalDate.now().minusMonths(2))
        whenever(disposal.expectedEndDate()).thenReturn(LocalDate.now().plusMonths(10))
        whenever(disposal.terminationDate).thenReturn(LocalDate.now().minusMonths(1))

        val event = mock<Event>()
        whenever(event.id).thenReturn(id)
        whenever(event.eventNumber).thenReturn(eventNumber)
        whenever(event.mainOffence).thenReturn(mainOffence)
        whenever(event.disposal).thenReturn(disposal)
        whenever(event.lastUpdatedDateTime).thenReturn(ZonedDateTime.now())
        whenever(event.isInactiveEvent()).thenReturn(inactive)
        return event
    }
}
