package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.data.generator.CourtAppearanceGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CourtRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.EventSentenceRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class SentenceServiceTest {

    @Mock
    lateinit var eventRepository: EventSentenceRepository

    @Mock
    lateinit var courtRepository: CourtRepository

    @InjectMocks
    lateinit var service: SentenceService

    @Test
    fun `no active sentences`() {

        whenever(eventRepository.findActiveSentencesByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(
            listOf()
        )

        val response = service.getMostRecentActiveEvent(PersonGenerator.OVERVIEW.crn)

        assertEquals(SentenceOverview(listOf()), response)
        verify(eventRepository, times(1)).findActiveSentencesByCrn(PersonGenerator.OVERVIEW.crn)

        verifyNoMoreInteractions(eventRepository)
        verifyNoInteractions(courtRepository)
    }

    @Test
    fun `recent active sentences`() {

        val event = PersonGenerator.generateEvent(
            person = PersonGenerator.OVERVIEW,
            active = true,
            inBreach = true,
            disposal = PersonGenerator.ACTIVE_ORDER,
            eventNumber = "123457",
            mainOffence = PersonGenerator.MAIN_OFFENCE_1,
            notes = "overview",
            additionalOffences = listOf(PersonGenerator.ADDITIONAL_OFFENCE_1)
        )

        whenever(eventRepository.findActiveSentencesByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(
            listOf(
                event
            )
        )

        whenever(courtRepository
            .getFirstCourtAppearanceByEventIdOrderByDate(event.id))
            .thenReturn(CourtAppearanceGenerator.generate())

        val response = service.getMostRecentActiveEvent(PersonGenerator.OVERVIEW.crn)

        val expected = SentenceOverview(
            listOf(
                Sentence(
                        OffenceDetails(
                            Offence("Murder", 1),
                            LocalDate.now(),
                            "overview",
                            listOf(
                                Offence("Burglary", 1)
                            )
                        ),
                        Conviction("Hull Court",
                            null,
                            null
                        )
                    )
                )
            )

        assertEquals(expected, response)
        verify(eventRepository, times(1)).findActiveSentencesByCrn(PersonGenerator.OVERVIEW.crn)

        verifyNoMoreInteractions(eventRepository)
    }
}