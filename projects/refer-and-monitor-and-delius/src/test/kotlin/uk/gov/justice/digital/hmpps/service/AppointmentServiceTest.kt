package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
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
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime
import java.util.Optional
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

    @Mock
    lateinit var telemetryService: TelemetryService

    @InjectMocks
    lateinit var appointmentService: AppointmentService

    @Test
    fun `using deliusId to find contact registers the same with app insights`() {
        val crn = "U123876"
        val referralId = UUID.randomUUID()
        val mergeAppointment = MergeAppointment(
            UUID.randomUUID(),
            referralId,
            "R1234EF",
            ZonedDateTime.now().plusMinutes(30),
            60,
            "some notes for the appointment",
            "DEFAULT",
            false,
            null,
            53L,
            null,
            9562746271,
            null,
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
            endTime = mergeAppointment.end,
            id = 9562746271
        )

        whenever(nsiRepository.findByPersonCrnAndExternalReference(crn, mergeAppointment.referralUrn))
            .thenReturn(nsi)
        whenever(providerService.findCrsAssignationDetails(mergeAppointment.officeLocationCode)).thenReturn(
            CrsAssignation(
                ProviderGenerator.INTENDED_PROVIDER,
                ProviderGenerator.INTENDED_TEAM,
                ProviderGenerator.INTENDED_STAFF,
                ProviderGenerator.DEFAULT_LOCATION
            )
        )
        whenever(contactRepository.findByPersonCrnAndExternalReference(crn, mergeAppointment.urn)).thenReturn(null)
        whenever(contactRepository.findById(contact.id)).thenReturn(Optional.of(contact))

        val id = assertDoesNotThrow { appointmentService.mergeAppointment(crn, mergeAppointment) }
        assertThat(id, equalTo(contact.id))

        verify(telemetryService).trackEvent(
            "Appointment Found By Delius Id - Merge Appointment",
            mapOf(
                "crn" to crn,
                "urn" to mergeAppointment.urn,
                "deliusId" to mergeAppointment.deliusId.toString(),
                "referralReference" to mergeAppointment.referralReference,
                "referralUrn" to mergeAppointment.referralUrn
            )
        )
    }
}
