package uk.gov.justice.digital.hmpps.epf

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.ManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.epf.entity.*

@ExtendWith(MockitoExtension::class)
internal class CaseDetailsServiceTest {
    @Mock
    internal lateinit var personRepository: PersonRepository

    @Mock
    internal lateinit var responsibleOfficerRepository: ResponsibleOfficerRepository

    @Mock
    internal lateinit var personManagerRepository: PersonManagerRepository

    @Mock
    internal lateinit var courtAppearanceRepository: CourtAppearanceRepository

    @Mock
    internal lateinit var eventRepository: EventRepository

    @Mock
    internal lateinit var ogrsAssessmentRepository: OgrsAssessmentRepository

    @InjectMocks
    internal lateinit var service: CaseDetailsService

    @Test
    fun `when active ro is not linked to active manager find com`() {
        val person = PersonGenerator.DEFAULT
        val event = SentenceGenerator.DEFAULT_EVENT
        whenever(personRepository.findByCrn(person.crn)).thenReturn(person)
        whenever(responsibleOfficerRepository.findByPersonIdAndEndDateIsNull(person.id)).thenReturn(null)
        whenever(personManagerRepository.findByPersonId(person.id)).thenReturn(ManagerGenerator.DEFAULT_PERSON_MANAGER)
        whenever(eventRepository.findEventByCrnAndEventNumber(person.crn, event.number)).thenReturn(event)
        whenever(courtAppearanceRepository.findMostRecentCourtNameByEventId(event.id)).thenReturn(SentenceGenerator.DEFAULT_COURT.name)

        val res = service.caseDetails(person.crn, event.number.toInt())
        assertNotNull(res.responsibleProvider)
        assertThat(res.responsibleProvider?.code, equalTo(ProviderGenerator.DEFAULT.code))
        assertThat(res.responsibleProvider?.name, equalTo(ProviderGenerator.DEFAULT.description))
    }
}