package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.FIRST_APPT_CONTACT
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.generateEvent
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class OverviewServiceTest {

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var requirementRepository: RequirementRepository

    @Mock
    lateinit var eventRepository: EventRepository

    @Mock
    lateinit var registrationRepository: RegistrationRepository

    @Mock
    lateinit var provisionRepository: ProvisionRepository

    @Mock
    lateinit var disabilityRepository: DisabilityRepository

    @Mock
    lateinit var personalCircumstanceRepository: PersonCircumstanceRepository

    @InjectMocks
    lateinit var service: OverviewService

    @Test
    fun `calls overview function`() {
        val crn = "X000004"
        val expectedAppointmentDateTime = ZonedDateTime.of(
            FIRST_APPT_CONTACT.date,
            FIRST_APPT_CONTACT.startTime.toLocalTime(),
            EuropeLondon
        )
        whenever(personRepository.findByCrn(crn)).thenReturn(PersonGenerator.OVERVIEW)

        whenever(registrationRepository.findByPersonId(any())).thenReturn(emptyList())
        whenever(provisionRepository.findByPersonId(any())).thenReturn(emptyList())
        whenever(disabilityRepository.findByPersonId(any())).thenReturn(emptyList())
        whenever(personalCircumstanceRepository.findByPersonId(any())).thenReturn(PersonGenerator.PERSONAL_CIRCUMSTANCES)

        whenever(contactRepository.findFirstAppointment(any(), any(), any(), any())).thenReturn(
            listOf(FIRST_APPT_CONTACT)
        )
        whenever(requirementRepository.getRarDays(any())).thenReturn(
            listOf(RarDays(1, "COMPLETED"), RarDays(2, "SCHEDULED"))
        )
        whenever(eventRepository.findByPersonId(PersonGenerator.OVERVIEW.id)).thenReturn(
            listOf(
                generateEvent(
                    person = PersonGenerator.OVERVIEW,
                    active = true,
                    inBreach = false,
                    disposal = PersonGenerator.ACTIVE_ORDER,
                    eventNumber = "654321",
                    mainOffence = PersonGenerator.MAIN_OFFENCE_1,
                    notes = "overview",
                    additionalOffences = emptyList()
                ),
                generateEvent(
                    person = PersonGenerator.OVERVIEW,
                    active = true,
                    inBreach = false,
                    disposal = PersonGenerator.ACTIVE_ORDER,
                    eventNumber = "654321",
                    mainOffence = PersonGenerator.MAIN_OFFENCE_2,
                    notes = "overview",
                    additionalOffences = emptyList()
                ),
                PersonGenerator.INACTIVE_EVENT_1,
                PersonGenerator.INACTIVE_EVENT_2
            )
        )
        val res = service.getOverview("X000004")
        assertThat(
            res.personalDetails.preferredName,
            equalTo(PersonGenerator.OVERVIEW.preferredName)
        )
        assertThat(
            res.personalDetails.personalCircumstances[0].type,
            equalTo(PersonGenerator.PERSONAL_CIRCUMSTANCES[0].type.description)
        )
        assertThat(res.sentences.size, equalTo(2))
        assertThat(res.sentences[0].rar?.scheduled, equalTo(2))
        assertThat(res.sentences[0].rar?.completed, equalTo(1))
        assertThat(res.sentences[0].rar?.totalDays, equalTo(3))
        assertThat(res.schedule.nextAppointment?.date, equalTo(expectedAppointmentDateTime))
    }

    data class RarDays(val _days: Int, val _type: String) :
        uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RarDays {
        override val days: Int
            get() = _days
        override val type: String
            get() = _type
    }
}