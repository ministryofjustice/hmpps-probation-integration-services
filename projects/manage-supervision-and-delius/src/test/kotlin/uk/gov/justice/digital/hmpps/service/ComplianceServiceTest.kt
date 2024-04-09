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
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.ACTIVE_ORDER
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.BREACH_PREVIOUS_ORDER_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.BREACH_PREVIOUS_ORDER_2
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.INACTIVE_ORDER_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RequirementRepository
import uk.gov.justice.digital.hmpps.utils.Summary
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
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

    @InjectMocks
    lateinit var service: ComplianceService

    private lateinit var personSummary: Summary

    @BeforeEach
    fun setup() {
        personSummary = Summary(
            id = OVERVIEW.id,
            forename = OVERVIEW.forename,
            secondName = OVERVIEW.secondName,
            surname = OVERVIEW.surname, crn = OVERVIEW.crn, pnc = OVERVIEW.pnc,
            dateOfBirth = OVERVIEW.dateOfBirth
        )
    }

    @Test
    fun `calls get compliance function`() {
        val crn = "X000005"
        val events = listOf(
            Event(
                id = BREACH_PREVIOUS_ORDER_1.eventId ?: 0,
                eventNumber = "1",
                disposal = INACTIVE_ORDER_1,
                active = false,
                additionalOffences = emptyList(),
                personId = OVERVIEW.id,
                convictionDate = LocalDate.now(),
                inBreach = false,
                notes = ""
            ),
            Event(
                id = BREACH_PREVIOUS_ORDER_2.eventId ?: 0,
                eventNumber = "2",
                disposal = INACTIVE_ORDER_1,
                active = false,
                additionalOffences = emptyList(),
                personId = OVERVIEW.id,
                convictionDate = LocalDate.now(),
                inBreach = false,
                notes = ""
            ),
            Event(
                id = 3,
                eventNumber = "3",
                disposal = ACTIVE_ORDER,
                active = true,
                mainOffence = PersonGenerator.MAIN_OFFENCE_1,
                additionalOffences = emptyList(),
                personId = OVERVIEW.id,
                convictionDate = LocalDate.now(),
                inBreach = false,
                notes = ""
            ),
            Event(
                id = 4,
                eventNumber = "4",
                disposal = ACTIVE_ORDER,
                active = true,
                mainOffence = PersonGenerator.MAIN_OFFENCE_2,
                additionalOffences = emptyList(),
                personId = OVERVIEW.id,
                convictionDate = LocalDate.now(),
                inBreach = false,
                notes = ""
            ),
        )

        whenever(requirementRepository.getRarDays(any())).thenReturn(
            listOf(RarDays(1, "COMPLETED"), OverviewServiceTest.RarDays(2, "SCHEDULED"))
        )

        whenever(personRepository.findSummary(crn)).thenReturn(personSummary)

        whenever(nsiRepository.findByPersonIdAndTypeCode(any(), any())).thenReturn(
            listOf(
                BREACH_PREVIOUS_ORDER_1,
                BREACH_PREVIOUS_ORDER_2
            )
        )

        whenever(activityService.getPersonSentenceActivity(any(), any())).thenReturn(
            listOfNotNull(
                ContactGenerator.NEXT_APPT_CONTACT.toActivity(),
                ContactGenerator.FIRST_NON_APPT_CONTACT.toActivity(),
                ContactGenerator.FIRST_APPT_CONTACT.toActivity(),
                ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.toActivity(),
                ContactGenerator.PREVIOUS_APPT_CONTACT.toActivity()
            )
        )

        whenever(eventRepository.findByPersonId(OVERVIEW.id)).thenReturn(events)

        val res = service.getPersonCompliance(crn)
        assertThat(res.personSummary, equalTo(OVERVIEW.toSummary()))
        assertThat(res.currentSentences.size, equalTo(2))
        assertThat(res.currentSentences[0].rar?.totalDays, equalTo(3))
        assertThat(res.previousOrders.breaches, equalTo(2))
        assertThat(res.previousOrders.orders.size, equalTo(2))
    }

    data class RarDays(val _days: Int, val _type: String) :
        uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RarDays {
        override val days: Int
            get() = _days
        override val type: String
            get() = _type
    }
}