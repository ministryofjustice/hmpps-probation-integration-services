package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.sentence.MainOffence
import uk.gov.justice.digital.hmpps.api.model.sentence.Offence
import uk.gov.justice.digital.hmpps.api.model.sentence.SentenceOverview
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.AdditionalOffence
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonOverviewRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.EventSentenceRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class SentenceServiceTest {

    @Mock
    lateinit var personRepository: PersonOverviewRepository

    @Mock
    lateinit var eventRepository: EventSentenceRepository

    @InjectMocks
    lateinit var service: SentenceService

    @Test
    fun `no active events`() {

        val person = PersonGenerator.OVERVIEW

        whenever(personRepository.findByCrn(person.crn)).thenReturn(PersonGenerator.OVERVIEW)

        whenever(eventRepository.findPersonById(person.id)).thenReturn(
            listOf(PersonGenerator.INACTIVE_EVENT_1)
        )

        val response = service.getMostRecentActiveEvent(person.crn)

        assertEquals(SentenceOverview(), response)
        verify(personRepository, times(1)).findByCrn(person.crn)
        verify(eventRepository, times(1)).findPersonById(person.id)

        verifyNoMoreInteractions(personRepository)
        verifyNoMoreInteractions(eventRepository)
    }

    @Test
    fun `recent active event`() {

        val person = PersonGenerator.OVERVIEW

        whenever(personRepository.findByCrn(person.crn)).thenReturn(PersonGenerator.OVERVIEW)

        whenever(eventRepository.findPersonById(person.id)).thenReturn(
            listOf(
                PersonGenerator.generateEvent(
                    person = PersonGenerator.OVERVIEW,
                    active = true,
                    inBreach = true,
                    disposal = PersonGenerator.ACTIVE_ORDER,
                    eventNumber = "123456",
                    mainOffence = PersonGenerator.MAIN_OFFENCE_2,
                    notes = "overview",
                    additionalOffences = emptyList()
                ),
                PersonGenerator.generateEvent(
                    person = PersonGenerator.OVERVIEW,
                    active = true,
                    inBreach = true,
                    disposal = PersonGenerator.ACTIVE_ORDER,
                    eventNumber = "123457",
                    mainOffence = PersonGenerator.MAIN_OFFENCE_1,
                    notes = "overview",
                    additionalOffences = listOf(PersonGenerator.ADDITIONAL_OFFENCE_1)
                ),
                PersonGenerator.INACTIVE_EVENT_1,
                PersonGenerator.INACTIVE_EVENT_2
            )
        )

        val response = service.getMostRecentActiveEvent(person.crn)

        val expected = SentenceOverview(
            MainOffence(
                Offence("Murder", 1),
                LocalDate.of(2024, 3, 11),
                "overview",
                listOf(Offence("Burglary", 1))
            )
        )
        assertEquals(expected, response)
        verify(personRepository, times(1)).findByCrn(person.crn)
        verify(eventRepository, times(1)).findPersonById(person.id)

        verifyNoMoreInteractions(personRepository)
        verifyNoMoreInteractions(eventRepository)
    }
}