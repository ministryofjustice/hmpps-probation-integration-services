package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
import uk.gov.justice.digital.hmpps.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
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
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var personManagerRepository: PersonManagerRepository

    @Mock
    lateinit var telemetryService: TelemetryService

    @InjectMocks
    lateinit var contactService: ContactService

    @Test
    fun `throws if person not found`() {
        whenever(contactRepository.existsByExternalReference("urn")).thenReturn(false)

        val exception = assertThrows<NotFoundException> { createContact() }

        assertThat(exception.message, equalTo("Person with crn of ${PERSON.crn} not found"))
    }

    @Test
    fun `throws if manager not found`() {
        whenever(contactRepository.existsByExternalReference("urn")).thenReturn(false)
        whenever(personRepository.findByCrn(PERSON.crn)).thenReturn(PERSON)

        val exception = assertThrows<NotFoundException> { createContact() }

        assertThat(exception.message, equalTo("Community manager with person id of ${PERSON.id} not found"))
    }

    @Test
    fun `throws if type not found`() {
        whenever(contactRepository.existsByExternalReference("urn")).thenReturn(false)
        whenever(personRepository.findByCrn(PERSON.crn)).thenReturn(PERSON)
        whenever(personManagerRepository.findByPersonId(PERSON.id)).thenReturn(MANAGER)

        val exception = assertThrows<NotFoundException> { createContact() }

        assertThat(exception.message, equalTo("Contact type with code of $REFERRAL_SUBMITTED not found"))
    }

    @Test
    fun `creates contact and adds audit`() {
        whenever(contactRepository.existsByExternalReference("urn")).thenReturn(false)
        whenever(personRepository.findByCrn(PERSON.crn)).thenReturn(PERSON)
        whenever(personManagerRepository.findByPersonId(PERSON.id)).thenReturn(MANAGER)
        whenever(contactTypeRepository.findByCode(REFERRAL_SUBMITTED)).thenReturn(CONTACT_TYPES[0])

        createContact()

        verify(contactRepository).save(check {
            assertThat(it.personId, equalTo(PERSON.id))
            assertThat(it.notes, equalTo("Some notes"))
            assertThat(it.externalReference, equalTo("urn"))
        })
        verify(auditedInteractionService).createAuditedInteraction(eq(ADD_CONTACT), any(), any())
    }

    @Test
    fun `does nothing when contact already exists`() {
        whenever(contactRepository.existsByExternalReference("urn")).thenReturn(true)

        createContact()

        verify(contactRepository, never()).save(any())
        verify(auditedInteractionService, never()).createAuditedInteraction(any(), any(), any())
        verify(telemetryService).trackEvent("ContactAlreadyExists", mapOf("urn" to "urn"), mapOf())
    }

    private fun createContact() {
        contactService.createContact(
            crn = PERSON.crn,
            type = REFERRAL_SUBMITTED,
            date = ZonedDateTime.now(),
            notes = "Some notes",
            urn = "urn"
        )
    }
}