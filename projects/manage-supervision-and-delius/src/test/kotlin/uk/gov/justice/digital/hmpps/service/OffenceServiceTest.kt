package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.offence.Offence
import uk.gov.justice.digital.hmpps.api.model.offence.OffenceDetails
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
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
    fun `no additional offences`() {
        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)
        whenever(
            eventRepository.getEventByPersonIdAndEventNumberAndActiveIsTrue(
                PersonGenerator.OVERVIEW.id,
                PersonGenerator.EVENT_1.eventNumber
            )
        ).thenReturn(
            null
        )

        val expected = OffenceDetails(name, null, null, listOf())
        val response = service.getOffencesForPerson(PersonGenerator.OVERVIEW.crn, PersonGenerator.EVENT_1.eventNumber)

        assertEquals(expected, response)
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
            PersonGenerator.MAIN_OFFENCE_1.date
        )
        val additionalOffence = Offence(
            PersonGenerator.ADDITIONAL_OFFENCE_1.offence.description,
            PersonGenerator.ADDITIONAL_OFFENCE_1.offence.category,
            PersonGenerator.ADDITIONAL_OFFENCE_1.offence.code,
            PersonGenerator.ADDITIONAL_OFFENCE_1.date
        )

        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)
        whenever(
            eventRepository.getEventByPersonIdAndEventNumberAndActiveIsTrue(
                PersonGenerator.OVERVIEW.id,
                PersonGenerator.EVENT_1.eventNumber
            )
        ).thenReturn(
            event
        )

        val expected = OffenceDetails(name, mainOffence, event.notes, listOf(additionalOffence))
        val response = service.getOffencesForPerson(PersonGenerator.OVERVIEW.crn, PersonGenerator.EVENT_1.eventNumber)

        assertEquals(expected, response)
    }
}