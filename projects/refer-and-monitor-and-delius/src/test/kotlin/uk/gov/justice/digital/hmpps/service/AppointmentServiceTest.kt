package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.MergeAppointment
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.EnforcementActionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.EnforcementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import java.time.ZonedDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
internal class AppointmentServiceTest {
    @Mock
    lateinit var auditedInteractionService: AuditedInteractionService

    @Mock
    lateinit var providerService: ProviderService

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var contactTypeRepository: ContactTypeRepository

    @Mock
    lateinit var outcomeRepository: ContactOutcomeRepository

    @Mock
    lateinit var enforcementActionRepository: EnforcementActionRepository

    @Mock
    lateinit var enforcementRepository: EnforcementRepository

    @Mock
    lateinit var eventRepository: EventRepository

    @Mock
    lateinit var nsiRepository: NsiRepository

    @InjectMocks
    lateinit var appointmentService: AppointmentService

    @Test
    fun `can fuzzy search nsi`() {
        val crn = "U123876"
        val referralId = UUID.randomUUID()
        val mergeAppointment = MergeAppointment(
            UUID.randomUUID(),
            referralId,
            "R1234EF",
            ZonedDateTime.now().plusMinutes(30),
            60,
            null,
            "some notes for the appointment",
            "DEFAULT",
            false,
            null,
            53L,
            null,
            null
        )
        val nsi = NsiGenerator.generate(
            NsiGenerator.TYPES.values.first(),
            eventId = mergeAppointment.sentenceId,
            notes = "urn:hmpps:interventions-referral:$referralId"
        )
        val contact = Contact(
            nsi.person,
            ContactGenerator.TYPES[ContactType.Code.CRSSAA.value]!!,
            providerId = ProviderGenerator.INTENDED_PROVIDER.id,
            teamId = ProviderGenerator.INTENDED_TEAM.id,
            staffId = ProviderGenerator.INTENDED_STAFF.id,
            locationId = ProviderGenerator.DEFAULT_LOCATION.id,
            eventId = nsi.eventId,
            nsiId = nsi.id,
            rarActivity = false,
            externalReference = mergeAppointment.urn,
            date = mergeAppointment.start.toLocalDate(),
            startTime = mergeAppointment.start,
            endTime = mergeAppointment.end
        )

        whenever(nsiRepository.fuzzySearch(eq(crn), eq(mergeAppointment.sentenceId!!), any()))
            .thenReturn(
                listOf(
                    nsi,
                    NsiGenerator.generate(NsiGenerator.TYPES.values.first(), eventId = mergeAppointment.sentenceId)
                )
            )
        whenever(providerService.findCrsAssignationDetails(mergeAppointment.officeLocationCode)).thenReturn(
            CrsAssignation(
                ProviderGenerator.INTENDED_PROVIDER,
                ProviderGenerator.INTENDED_TEAM,
                ProviderGenerator.INTENDED_STAFF,
                ProviderGenerator.DEFAULT_LOCATION
            )
        )
        whenever(contactRepository.findByPersonCrnAndExternalReference(crn, mergeAppointment.urn)).thenReturn(contact)

        val id = assertDoesNotThrow { appointmentService.mergeAppointment(crn, mergeAppointment) }
        assertThat(id, equalTo(contact.id))
    }
}
