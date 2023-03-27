package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.DataIntegrityViolationException
import uk.gov.justice.digital.hmpps.api.model.ReferralStarted
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.exception.FutureAppointmentLinkedException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus
import uk.gov.justice.digital.hmpps.messaging.NsiTermination
import uk.gov.justice.digital.hmpps.messaging.ReferralEndType
import java.time.ZonedDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
internal class NsiServiceTest {
    @Mock
    lateinit var auditedInteractionService: AuditedInteractionService

    @Mock
    lateinit var nsiRepository: NsiRepository

    @Mock
    lateinit var nsiStatusRepository: NsiStatusRepository

    @Mock
    lateinit var nsiOutcomeRepository: NsiOutcomeRepository

    @Mock
    lateinit var statusHistoryRepository: NsiStatusHistoryRepository

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var contactTypeRepository: ContactTypeRepository

    @Mock
    lateinit var createNsi: CreateNsi

    @InjectMocks
    lateinit var nsiService: NsiService

    @Test
    fun `nsi terminated by delius ui is set to commenced after receiving started`() {
        val crn = "T123456"
        val referralId = UUID.randomUUID()
        val ref = "urn:hmpps:interventions-referral:$referralId"
        val nsi = NsiGenerator.generate(NsiGenerator.TYPES.values.first(), status = NsiGenerator.COMP_STATUS)
        val manager = NsiGenerator.generateManager(nsi)
        whenever(nsiRepository.findByPersonCrnAndExternalReference(crn, ref))
            .thenReturn(nsi.withManager(manager))
        whenever(nsiStatusRepository.findByCode(NsiStatus.Code.IN_PROGRESS.value)).thenReturn(NsiGenerator.INPROG_STATUS)
        whenever(contactTypeRepository.getReferenceById(NsiGenerator.INPROG_STATUS.contactTypeId))
            .thenReturn(ContactGenerator.TYPES[ContactType.Code.IN_PROGRESS.value])

        nsiService.startNsi(
            crn,
            ReferralStarted(referralId, nsi.referralDate, "E2E", 197L, "some notes about this referral")
        )

        verify(statusHistoryRepository).save(any())
        verify(contactRepository).save(any())
    }

    @Test
    fun `unable to delete future appointments due to linked contacts`() {
        val person = PersonGenerator.DEFAULT
        val nsi = NsiGenerator.generate(
            NsiGenerator.TYPES.values.first(),
            eventId = 3789182,
            externalReference = UUID.randomUUID().toString()
        )
        val manager = NsiGenerator.generateManager(nsi)
        whenever(nsiRepository.findByPersonCrnAndExternalReference(person.crn, nsi.externalReference!!))
            .thenReturn(nsi.withManager(manager))
        whenever(nsiStatusRepository.findByCode(NsiStatus.Code.END.value)).thenReturn(NsiGenerator.COMP_STATUS)
        whenever(nsiOutcomeRepository.findByCode(ReferralEndType.COMPLETED.outcome, Dataset.Code.NSI_OUTCOME.value))
            .thenReturn(NsiGenerator.OUTCOMES[ReferralEndType.COMPLETED.outcome])
        whenever(contactRepository.deleteFutureAppointmentsForNsi(nsi.id))
            .thenThrow(DataIntegrityViolationException::class.java)

        val termination = NsiTermination(
            person.crn,
            nsi.externalReference!!,
            nsi.eventId!!,
            nsi.referralDate,
            ZonedDateTime.now(),
            ReferralEndType.COMPLETED,
            "This referral has been completed"
        )

        assertThrows<FutureAppointmentLinkedException> { nsiService.terminateNsi(termination) }
    }
}
