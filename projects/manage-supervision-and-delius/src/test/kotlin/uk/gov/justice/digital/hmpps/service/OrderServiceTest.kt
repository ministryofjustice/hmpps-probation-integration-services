package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.sentence.PreviousOrder
import uk.gov.justice.digital.hmpps.api.model.sentence.PreviousOrderHistory
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.EventSentenceRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class OrderServiceTest {
    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var eventRepository: EventSentenceRepository

    @InjectMocks
    lateinit var service: OrdersService

    @Test
    fun `no previous orders`() {
        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)
        whenever(eventRepository.findSentencesByPersonId(PersonGenerator.OVERVIEW.id)).thenReturn(
            listOf()
        )

        val expected = PreviousOrderHistory(listOf())
        val response = service.getPreviousEvents(PersonGenerator.OVERVIEW.crn)

        assertEquals(expected, response)
    }

    @Test
    fun `return previous orders`() {
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

        whenever(personRepository.findByCrn(PersonGenerator.OVERVIEW.crn)).thenReturn(PersonGenerator.OVERVIEW)
        whenever(eventRepository.findSentencesByPersonId(PersonGenerator.OVERVIEW.id)).thenReturn(listOf(event))

        val expected = PreviousOrderHistory(
            listOf(
                PreviousOrder(
                    "Murder (25 Years)",
                    "Default Sentence Type",
                    LocalDate.now().minusDays(8)
                )
            )
        )
        val response = service.getPreviousEvents(PersonGenerator.OVERVIEW.crn)

        assertEquals(expected, response)
    }
}