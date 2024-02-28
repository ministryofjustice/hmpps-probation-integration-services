package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.generateEvent
import uk.gov.justice.digital.hmpps.integrations.delius.overview.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.PersonOverviewRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.RequirementRepository

@ExtendWith(MockitoExtension::class)
internal class OverviewServiceTest {

    @Mock
    lateinit var personRepository: PersonOverviewRepository

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var requirementRepository: RequirementRepository

    @Mock
    lateinit var eventRepository: EventRepository

    @InjectMocks
    lateinit var service: OverviewService

    @Test
    fun `calls overview function`() {
        val crn = "X000004"

        whenever(personRepository.findByCrn(crn)).thenReturn(PersonGenerator.OVERVIEW)
        whenever(contactRepository.findFirstAppointment(PersonGenerator.OVERVIEW.id)).thenReturn(listOf(ContactGenerator.FIRST_APPT_CONTACT))
        whenever(requirementRepository.getRarDays(any())).thenReturn(
            listOf(RarDays(1, "COMPLETED"), RarDays(2, "SCHEDULED"))
        )
        whenever(eventRepository.findByPersonId(PersonGenerator.OVERVIEW.id)).thenReturn(
            listOf(
                generateEvent(
                    person = PersonGenerator.OVERVIEW,
                    active = true,
                    inBreach = false,
                    disposal = PersonGenerator.ACTIVE_ORDER
                ),
                PersonGenerator.EVENT_2,
                PersonGenerator.INACTIVE_EVENT_1,
                PersonGenerator.INACTIVE_EVENT_2
            )
        )
        val res = service.getOverview("X000004")
        assertThat(
            res.personalDetails.preferredName,
            equalTo(PersonGenerator.OVERVIEW.preferredName)
        )
        assertThat(
            res.personalDetails.personalCircumstances[0].type,
            equalTo(PersonGenerator.OVERVIEW.personalCircumstances[0].type.description)
        )
        assertThat(res.sentences.size, equalTo(2))
        assertThat(res.sentences[0].rar?.scheduled, equalTo(2))
        assertThat(res.sentences[0].rar?.completed, equalTo(1))
        assertThat(res.sentences[0].rar?.totalDays, equalTo(3))
    }

    data class RarDays(val _days: Int, val _type: String) :
        uk.gov.justice.digital.hmpps.integrations.delius.overview.RarDays {
        override val days: Int
            get() = _days
        override val type: String
            get() = _type
    }
}