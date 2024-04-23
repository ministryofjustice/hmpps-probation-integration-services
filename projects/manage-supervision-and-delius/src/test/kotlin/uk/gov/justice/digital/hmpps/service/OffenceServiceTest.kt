package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.Name
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

    @Test
    fun `no previous offences`() {
        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)
        whenever(
            eventRepository.getEventByPersonIdAndEventNumberAndActiveIsTrue(
                PersonGenerator.OVERVIEW.id,
                PersonGenerator.EVENT_1.eventNumber
            )
        ).thenReturn(
            null
        )

        val expected = OffenceDetails(Name("Forename", "Middle1", "Surname"), null, null, listOf())
        val response = service.getOffencesForPerson(PersonGenerator.OVERVIEW.crn, PersonGenerator.EVENT_1.eventNumber)

        assertEquals(expected, response)
    }
}