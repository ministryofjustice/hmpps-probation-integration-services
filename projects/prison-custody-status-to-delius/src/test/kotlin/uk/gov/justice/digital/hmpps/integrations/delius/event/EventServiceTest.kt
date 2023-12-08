package uk.gov.justice.digital.hmpps.integrations.delius.event

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class EventServiceTest {
    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var eventRepository: EventRepository

    @InjectMocks
    lateinit var eventService: EventService

    @Test
    fun activeCustodialEventIsReturned() {
        val event = EventGenerator.custodialEvent(PersonGenerator.RELEASABLE, InstitutionGenerator.DEFAULT)
        whenever(personRepository.findByNomsNumberAndSoftDeletedIsFalse(PersonGenerator.RELEASABLE.nomsNumber))
            .thenReturn(listOf(PersonGenerator.RELEASABLE))
        whenever(eventRepository.findActiveCustodialEvents(PersonGenerator.RELEASABLE.id))
            .thenReturn(listOf(event))

        assertEquals(listOf(event), eventService.getActiveCustodialEvents(PersonGenerator.RELEASABLE.nomsNumber))
    }

    @Test
    fun missingNomsNumberIsIgnored() {
        whenever(personRepository.findByNomsNumberAndSoftDeletedIsFalse(PersonGenerator.RELEASABLE.nomsNumber))
            .thenReturn(emptyList())
        assertThrows<IgnorableMessageException> {
            eventService.getActiveCustodialEvents(PersonGenerator.RELEASABLE.nomsNumber)
        }
    }

    @Test
    fun duplicateNomsNumberIsIgnored() {
        whenever(personRepository.findByNomsNumberAndSoftDeletedIsFalse(PersonGenerator.RELEASABLE.nomsNumber))
            .thenReturn(List(3) { PersonGenerator.RELEASABLE })
        assertThrows<IgnorableMessageException> {
            eventService.getActiveCustodialEvents(PersonGenerator.RELEASABLE.nomsNumber)
        }
    }

    @Test
    fun noActiveCustodialEventIsIgnored() {
        whenever(personRepository.findByNomsNumberAndSoftDeletedIsFalse(PersonGenerator.RELEASABLE.nomsNumber))
            .thenReturn(listOf(PersonGenerator.RELEASABLE))
        whenever(eventRepository.findActiveCustodialEvents(PersonGenerator.RELEASABLE.id)).thenReturn(emptyList())
        assertThrows<IgnorableMessageException> {
            eventService.getActiveCustodialEvents(PersonGenerator.RELEASABLE.nomsNumber)
        }
    }

    @Test
    fun newReleaseSetsReleaseDate() {
        val releaseDate = ZonedDateTime.now()
        val event = EventGenerator.custodialEvent(PersonGenerator.RELEASABLE, InstitutionGenerator.DEFAULT)

        eventService.updateReleaseDateAndIapsFlag(event, releaseDate)

        assertThat(event.firstReleaseDate, equalTo(releaseDate))
        verify(eventRepository).save(event)
    }

    @Test
    fun subsequentReleaseDoesntOverrideFirstReleaseDate() {
        val firstReleaseDate = ZonedDateTime.now().minusWeeks(1)
        val releaseDate = ZonedDateTime.now()
        val event =
            EventGenerator.previouslyReleasedEvent(
                PersonGenerator.RELEASABLE,
                InstitutionGenerator.DEFAULT,
                releaseDate = firstReleaseDate,
            )

        eventService.updateReleaseDateAndIapsFlag(event, releaseDate)

        assertThat(event.firstReleaseDate, equalTo(firstReleaseDate))
        verify(eventRepository, never()).save(event)
    }
}
