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
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonOverviewRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.EventSentenceRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class SentenceServiceTest {

    @Mock
    lateinit var eventRepository: EventSentenceRepository

    @InjectMocks
    lateinit var service: SentenceService

    @Test
    fun `no active events`() {

        whenever(eventRepository.findActiveSentencesByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(
            listOf(PersonGenerator.INACTIVE_EVENT_1)
        )

        val response = service.getMostRecentActiveEvent(PersonGenerator.OVERVIEW.crn)

        assertEquals(SentenceOverview(), response)
        verify(eventRepository, times(1)).findActiveSentencesByCrn(PersonGenerator.OVERVIEW.crn)

        verifyNoMoreInteractions(eventRepository)
    }

    @Test
    fun `recent active event`() {

        whenever(eventRepository.findActiveSentencesByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(
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
                )
            )
        )

        val response = service.getMostRecentActiveEvent(PersonGenerator.OVERVIEW.crn)

        val expected = SentenceOverview(
            MainOffence(
                Offence("Murder", 1),
                LocalDate.of(2024, 3, 12),
                "overview",
                listOf(Offence("Burglary", 1))
            )
        )
        assertEquals(expected, response)
        verify(eventRepository, times(1)).findActiveSentencesByCrn(PersonGenerator.OVERVIEW.crn)

        verifyNoMoreInteractions(eventRepository)
    }
}