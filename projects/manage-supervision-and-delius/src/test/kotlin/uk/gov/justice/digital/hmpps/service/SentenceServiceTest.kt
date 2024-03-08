package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.sentence.SentenceOverview
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonOverviewRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.EventSentenceRepository

@ExtendWith(MockitoExtension::class)
class SentenceServiceTest {

    @Mock
    lateinit var personRepository: PersonOverviewRepository

    @Mock
    lateinit var eventRepository: EventSentenceRepository

    @InjectMocks
    lateinit var service: SentenceService

    @Test
    fun `get most recent active event`() {
        val person = PersonGenerator.OVERVIEW


    }
}