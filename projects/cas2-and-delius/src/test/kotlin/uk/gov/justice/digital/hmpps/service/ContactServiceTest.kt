package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.EntityGenerator.CONTACT_TYPES
import uk.gov.justice.digital.hmpps.data.generator.EntityGenerator.MANAGER
import uk.gov.justice.digital.hmpps.data.generator.EntityGenerator.PERSON
import uk.gov.justice.digital.hmpps.entity.ContactRepository
import uk.gov.justice.digital.hmpps.entity.ContactType.Companion.REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.ADD_CONTACT
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class ContactServiceTest {
    @Mock
    lateinit var auditedInteractionService: AuditedInteractionService

    @Mock(answer = Answers.RETURNS_MOCKS)
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var contactTypeRepository: ContactTypeRepository

    @Mock
    lateinit var telemetryService: TelemetryService

    @InjectMocks
    lateinit var contactService: ContactService

    @Test
    fun `creates contact and adds audit`() {
        whenever(contactRepository.existsByExternalReference("new-urn")).thenReturn(false)
        whenever(contactTypeRepository.findByCode(REFERRAL_SUBMITTED)).thenReturn(CONTACT_TYPES[0])

        contactService.createContact(
            personId = PERSON.id,
            type = REFERRAL_SUBMITTED,
            date = ZonedDateTime.now(),
            manager = MANAGER,
            notes = "Some notes",
            urn = "new-urn"
        )

        verify(contactRepository).save(check {
            assertThat(it.personId, equalTo(PERSON.id))
            assertThat(it.notes, equalTo("Some notes"))
            assertThat(it.externalReference, equalTo("new-urn"))
        })
        verify(auditedInteractionService).createAuditedInteraction(eq(ADD_CONTACT), any(), any())
    }

    @Test
    fun `does nothing when contact already exists`() {
        whenever(contactRepository.existsByExternalReference("existing-urn")).thenReturn(true)

        contactService.createContact(
            personId = PERSON.id,
            type = REFERRAL_SUBMITTED,
            date = ZonedDateTime.now(),
            manager = MANAGER,
            notes = "Some notes",
            urn = "existing-urn"
        )

        verify(contactRepository, never()).save(any())
        verify(auditedInteractionService, never()).createAuditedInteraction(any(), any(), any())
        verify(telemetryService).trackEvent("ContactAlreadyExists", mapOf("urn" to "existing-urn"), mapOf())
    }
}