package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.ReferralStarted
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiStatusRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.NsiStatus
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class NsiServiceTest {
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
}
