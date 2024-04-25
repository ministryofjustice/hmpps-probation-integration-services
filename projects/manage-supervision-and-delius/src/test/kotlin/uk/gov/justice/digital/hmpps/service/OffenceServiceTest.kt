package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.offence.Offence
import uk.gov.justice.digital.hmpps.api.model.offence.Offences
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.EventSentenceRepository

@ExtendWith(MockitoExtension::class)
class OffenceServiceTest {
    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var eventRepository: EventSentenceRepository

    @InjectMocks
    lateinit var service: OffenceService

    val name = Name(
        PersonGenerator.OVERVIEW.forename,
        PersonGenerator.OVERVIEW.secondName,
        PersonGenerator.OVERVIEW.surname
    )

    @Test
    fun `person does not exist`() {
        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenThrow(
            NotFoundException(
                "Person",
                "crn",
                PersonGenerator.OVERVIEW.crn
            )
        )

        val exception = assertThrows<NotFoundException> {
            service.getOffencesForPerson(PersonGenerator.OVERVIEW.crn, PersonGenerator.EVENT_1.eventNumber)
        }

        assertEquals("Person with crn of X000004 not found", exception.message)

        verify(personRepository, times(1)).findByCrn(PersonGenerator.OVERVIEW.crn)

        verifyNoMoreInteractions(personRepository)
        verifyNoInteractions(eventRepository)
    }

    @Test
    fun `no active events`() {
        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)
        whenever(
            eventRepository.findEventByPersonIdAndEventNumberAndActiveIsTrue(
                PersonGenerator.OVERVIEW.id,
                PersonGenerator.EVENT_1.eventNumber
            )
        ).thenThrow(NotFoundException("Event", "number", PersonGenerator.EVENT_1.eventNumber))

        val exception = assertThrows<NotFoundException> {
            service.getOffencesForPerson(PersonGenerator.OVERVIEW.crn, PersonGenerator.EVENT_1.eventNumber)
        }

        assertEquals("Event with number of 7654321 not found", exception.message)

        verify(personRepository, times(1)).findByCrn(PersonGenerator.OVERVIEW.crn)
        verify(eventRepository, times(1)).findEventByPersonIdAndEventNumberAndActiveIsTrue(
            PersonGenerator.OVERVIEW.id,
            PersonGenerator.EVENT_1.eventNumber
        )

        verifyNoMoreInteractions(personRepository)
        verifyNoMoreInteractions(eventRepository)
    }

    @Test
    fun `return additional offences`() {
        val event = PersonGenerator.generateEvent(
            person = PersonGenerator.OVERVIEW,
            active = false,
            inBreach = true,
            disposal = PersonGenerator.INACTIVE_ORDER_1,
            eventNumber = "123457",
            mainOffence = PersonGenerator.MAIN_OFFENCE_1,
            notes = "overview",
            additionalOffences = listOf(PersonGenerator.ADDITIONAL_OFFENCE_1)
        )

        val mainOffence = Offence(
            PersonGenerator.MAIN_OFFENCE_1.offence.description,
            PersonGenerator.MAIN_OFFENCE_1.offence.category,
            PersonGenerator.MAIN_OFFENCE_1.offence.code,
            PersonGenerator.MAIN_OFFENCE_1.date,
            1,
            event.notes
        )
        val additionalOffence = Offence(
            PersonGenerator.ADDITIONAL_OFFENCE_1.offence.description,
            PersonGenerator.ADDITIONAL_OFFENCE_1.offence.category,
            PersonGenerator.ADDITIONAL_OFFENCE_1.offence.code,
            PersonGenerator.ADDITIONAL_OFFENCE_1.date,
            1,
            null
        )

        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)
        whenever(
            eventRepository.findEventByPersonIdAndEventNumberAndActiveIsTrue(
                PersonGenerator.OVERVIEW.id,
                PersonGenerator.EVENT_1.eventNumber
            )
        ).thenReturn(
            event
        )

        val expected = Offences(name, mainOffence, listOf(additionalOffence))
        val response = service.getOffencesForPerson(PersonGenerator.OVERVIEW.crn, PersonGenerator.EVENT_1.eventNumber)

        assertEquals(expected, response)

        verify(personRepository, times(1)).findByCrn(PersonGenerator.OVERVIEW.crn)
        verify(eventRepository, times(1)).findEventByPersonIdAndEventNumberAndActiveIsTrue(
            PersonGenerator.OVERVIEW.id,
            PersonGenerator.EVENT_1.eventNumber
        )

        verifyNoMoreInteractions(personRepository)
        verifyNoMoreInteractions(eventRepository)
    }
}