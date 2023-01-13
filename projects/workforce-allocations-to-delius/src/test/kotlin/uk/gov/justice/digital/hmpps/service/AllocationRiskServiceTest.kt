package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.OasysAssessmentGenerator
import uk.gov.justice.digital.hmpps.data.generator.OgrsAssessmentGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.ogrs.OASYSAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.ogrs.OGRSAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.registration.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository

@ExtendWith(MockitoExtension::class)
class AllocationRiskServiceTest {
    @Mock lateinit var registrationRepository: RegistrationRepository
    @Mock lateinit var ogrsAssessmentRepository: OGRSAssessmentRepository
    @Mock lateinit var oasysAssessmentRepository: OASYSAssessmentRepository
    @Mock lateinit var eventRepository: EventRepository
    @Mock lateinit var personRepository: PersonRepository
    @InjectMocks lateinit var allocationRiskService: AllocationRiskService

    @Test
    fun `person not found`() {
        val exception = assertThrows<NotFoundException> {
            allocationRiskService.getRiskRecord("UNK", "1")
        }
        assertThat(exception.message, equalTo("Person with crn of UNK not found"))
    }

    @Test
    fun `Risk record retrieved event not found`() {
        val person = PersonGenerator.DEFAULT
        val event = EventGenerator.DEFAULT
        whenever(personRepository.findByCrnAndSoftDeletedFalse(person.crn)).thenReturn(person)

        val exception = assertThrows<NotFoundException> {
            allocationRiskService.getRiskRecord(person.crn, event.number)
        }
        assertThat(exception.message, equalTo("Event with crn of ${person.crn} not found"))
    }

    @Test
    fun `Risk record retrieved no registrations or ogrs`() {
        val person = PersonGenerator.DEFAULT
        val event = EventGenerator.DEFAULT
        whenever(personRepository.findByCrnAndSoftDeletedFalse(person.crn)).thenReturn(person)
        whenever(eventRepository.findByPersonCrnAndNumber(person.crn, event.number)).thenReturn(event)

        val response = allocationRiskService.getRiskRecord(person.crn, event.number)

        assertThat(response.name.forename, equalTo(person.forename))
        assertThat(response.name.surname, equalTo(person.surname))
        assertThat(response.activeRegistrations.size, equalTo(0))
        assertThat(response.inactiveRegistrations.size, equalTo(0))
    }

    @Test
    fun `Risk record retrieved with active registrations and ogrs`() {
        val person = PersonGenerator.DEFAULT
        val event = EventGenerator.DEFAULT
        val registration = RegistrationGenerator.DEFAULT

        whenever(personRepository.findByCrnAndSoftDeletedFalse(person.crn)).thenReturn(person)
        whenever(eventRepository.findByPersonCrnAndNumber(person.crn, event.number)).thenReturn(event)
        whenever(registrationRepository.findAllByPersonCrn(person.crn)).thenReturn(listOf(registration))
        whenever(
            oasysAssessmentRepository.findByPersonIdAndEventNumberOrderByAssessmentDateDesc(
                person.id,
                event.number
            )
        ).thenReturn(OasysAssessmentGenerator.DEFAULT)
        val response = allocationRiskService.getRiskRecord(person.crn, event.number)

        assertThat(response.name.forename, equalTo(person.forename))
        assertThat(response.name.surname, equalTo(person.surname))
        assertThat(response.activeRegistrations.size, equalTo(1))
        assertThat(response.inactiveRegistrations.size, equalTo(0))
        assertThat(response.ogrs!!.score, equalTo(OasysAssessmentGenerator.DEFAULT.score))
        assertThat(response.ogrs!!.lastUpdatedDate, equalTo(OasysAssessmentGenerator.DEFAULT.lastModifiedDateTime.toLocalDate()))
    }
    @Test
    fun `Risk record retrieved with inactive registrations and ogrs`() {
        val person = PersonGenerator.DEFAULT
        val event = EventGenerator.DEFAULT
        val registration = RegistrationGenerator.WITH_DEREGISTRATION

        whenever(personRepository.findByCrnAndSoftDeletedFalse(person.crn)).thenReturn(person)
        whenever(eventRepository.findByPersonCrnAndNumber(person.crn, event.number)).thenReturn(event)
        whenever(registrationRepository.findAllByPersonCrn(person.crn)).thenReturn(listOf(registration))
        whenever(
            ogrsAssessmentRepository.findByEventIdOrderByAssessmentDateDesc(
                event.id
            )
        ).thenReturn(OgrsAssessmentGenerator.DEFAULT)
        val response = allocationRiskService.getRiskRecord(person.crn, event.number)

        assertThat(response.name.forename, equalTo(person.forename))
        assertThat(response.name.surname, equalTo(person.surname))
        assertThat(response.activeRegistrations.size, equalTo(0))
        assertThat(response.inactiveRegistrations.size, equalTo(1))
        assertThat(response.ogrs!!.score, equalTo(OgrsAssessmentGenerator.DEFAULT.score))
        assertThat(response.ogrs!!.lastUpdatedDate, equalTo(OgrsAssessmentGenerator.DEFAULT.lastModifiedDateTime.toLocalDate()))
    }
}
