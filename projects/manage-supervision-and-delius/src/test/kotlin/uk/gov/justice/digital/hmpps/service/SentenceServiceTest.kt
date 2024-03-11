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
import uk.gov.justice.digital.hmpps.api.model.sentence.SentenceOverview
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonOverviewRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.EventSentenceRepository

@ExtendWith(MockitoExtension::class)
class SentenceServiceTest() {

    @Mock
    lateinit var personRepository: PersonOverviewRepository

    @Mock
    lateinit var eventRepository: EventSentenceRepository

    @InjectMocks
    lateinit var service: SentenceService

    @Test
    fun `recent active event, with no offence`() {

        val person = PersonGenerator.OVERVIEW

        whenever(personRepository.findByCrn(person.crn)).thenReturn(PersonGenerator.OVERVIEW)

        whenever(eventRepository.findPersonById(person.id)).thenReturn(
            listOf(Event(id = 11, personId = person.id, eventNumber = "123", inBreach = true, active = true, notes = "notes"))
        )

        val response = service.getMostRecentActiveEvent(person.crn)

        assertEquals(SentenceOverview(), response)
        verify(personRepository, times(1)).findByCrn(person.crn)
        verify(eventRepository, times(1)).findPersonById(person.id)

        verifyNoMoreInteractions(personRepository)
        verifyNoMoreInteractions(eventRepository)
    }
}