package uk.gov.justice.digital.hmpps.integrations.delius.licencecondition

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionTransferGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataSetGenerator
import uk.gov.justice.digital.hmpps.data.generator.withManager
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactDetail
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactService
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceConditionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceConditionTransfer
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceConditionTransferRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.TransferStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.transfer.entity.RejectedTransferDiaryRepository
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class LicenceConditionServiceTest {
    @Mock
    lateinit var licenceConditionRepository: LicenceConditionRepository

    @Mock
    lateinit var licenceConditionTransferRepository: LicenceConditionTransferRepository

    @Mock
    lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    lateinit var rejectedTransferDiaryRepository: RejectedTransferDiaryRepository

    @Mock
    lateinit var contactService: ContactService

    @InjectMocks
    lateinit var licenceConditionService: LicenceConditionService

    @Test
    fun nothingIsDoneWhenThereAreNoLicenceConditions() {
        licenceConditionService.terminateLicenceConditionsForDisposal(
            1L,
            ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON,
            ZonedDateTime.now()
        )
        verify(licenceConditionRepository, never()).save(any())
        verify(referenceDataRepository, never()).findByCodeAndSetName(any(), any())
    }

    @Test
    fun missingTransferStatusIsThrown() {
        val lc = LicenceConditionGenerator.DEFAULT.withManager()
        withLicenceConditions(licenceConditions = listOf(lc))
        withLicenceConditionTransfers(lc)
        val exception = assertThrows<NotFoundException> {
            licenceConditionService.terminateLicenceConditionsForDisposal(
                1L,
                ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON,
                ZonedDateTime.now()
            )
        }
        assertEquals("TRANSFER STATUS with code of TR not found", exception.message)
    }

    @Test
    fun missingRejectedDecisionIsThrown() {
        val lc = LicenceConditionGenerator.DEFAULT.withManager()
        withLicenceConditions(licenceConditions = listOf(lc))
        withLicenceConditionTransfers(lc)
        withReferenceData(
            ReferenceDataGenerator.TRANSFER_STATUS[TransferStatusCode.REJECTED]
        )

        val exception = assertThrows<NotFoundException> {
            licenceConditionService.terminateLicenceConditionsForDisposal(
                1L,
                ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON,
                ZonedDateTime.now()
            )
        }
        assertEquals("ACCEPTED DECISION with code of R not found", exception.message)
    }

    @Test
    fun missingRejectionReasonIsThrown() {
        val lc = LicenceConditionGenerator.DEFAULT.withManager()
        withLicenceConditions(licenceConditions = listOf(lc))
        withLicenceConditionTransfers(lc)
        withReferenceData(
            ReferenceDataGenerator.TRANSFER_STATUS[TransferStatusCode.REJECTED],
            ReferenceDataGenerator.generate("R", ReferenceDataSetGenerator.generate("ACCEPTED DECISION"))
        )

        val exception = assertThrows<NotFoundException> {
            licenceConditionService.terminateLicenceConditionsForDisposal(
                1L,
                ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON,
                ZonedDateTime.now()
            )
        }
        assertEquals("LICENCE AREA TRANSFER REJECTION REASON with code of TWR not found", exception.message)
    }

    @Test
    fun licenceConditionsAreTerminated() {
        val now = ZonedDateTime.now()
        val event =
            EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT).withManager()
        val licenceConditions = List(3) { LicenceConditionGenerator.generate(event).withManager() }
        withLicenceConditions(event, licenceConditions)

        licenceConditionService.terminateLicenceConditionsForDisposal(
            event.disposal!!.id,
            ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON,
            now,
            true
        )

        licenceConditions.forEach {
            assertEquals(it.terminationDate, now)
            assertEquals(it.terminationReason, ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON)
            assertFalse(it.pendingTransfer!!)
            verify(licenceConditionRepository).save(it)
            verify(contactService).deleteFutureDatedLicenceConditionContacts(it.id, it.terminationDate!!)
            verify(contactService).createContact(
                ContactDetail(
                    ContactType.Code.COMPONENT_TERMINATED,
                    it.terminationDate!!,
                    "Termination reason: ${it.terminationReason!!.description}${LicenceConditionService.EOTL_TERMINATE_LICENCE_CONTACT_NOTES}"
                ),
                it.disposal.event.person,
                it.disposal.event,
                it.manager,
                it.id
            )
        }
    }

    @Test
    fun pendingTransfersAreTerminated() {
        val now = ZonedDateTime.now()
        val event =
            EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT).withManager()
        val licenceCondition = LicenceConditionGenerator.generate(event).withManager()
        val licenceConditionTransfers = List(3) { LicenceConditionTransferGenerator.generate(licenceCondition) }
        withLicenceConditions(event, listOf(licenceCondition))
        withLicenceConditionTransfers(licenceCondition, licenceConditionTransfers)
        withReferenceData(
            ReferenceDataGenerator.TRANSFER_STATUS[TransferStatusCode.REJECTED],
            ReferenceDataGenerator.generate("R", ReferenceDataSetGenerator.generate("ACCEPTED DECISION")),
            ReferenceDataGenerator.generate(
                "TWR",
                ReferenceDataSetGenerator.generate("LICENCE AREA TRANSFER REJECTION REASON")
            )
        )

        licenceConditionService.terminateLicenceConditionsForDisposal(
            event.disposal!!.id,
            ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON,
            now
        )

        licenceConditionTransfers.forEach {
            assertEquals(it.status, ReferenceDataGenerator.TRANSFER_STATUS[TransferStatusCode.REJECTED])
            assertEquals(it.statusDate, now)
        }
        verify(licenceConditionTransferRepository).saveAll(licenceConditionTransfers)
    }

    private fun withLicenceConditionTransfers(
        licenceCondition: LicenceCondition,
        transfers: List<LicenceConditionTransfer> = List(3) {
            LicenceConditionTransferGenerator.generate(
                licenceCondition
            )
        }
    ) {
        whenever(
            licenceConditionTransferRepository.findAllByLicenceConditionIdAndStatusCode(
                licenceCondition.id,
                TransferStatusCode.PENDING.code
            )
        ).thenReturn(transfers)
    }

    private fun withLicenceConditions(
        event: Event,
        licenceConditions: List<LicenceCondition> = List(3) { LicenceConditionGenerator.generate(event.withManager()) }
    ) {
        withLicenceConditions(event.disposal!!.id, licenceConditions)
    }

    private fun withLicenceConditions(
        disposalId: Long = 1L,
        licenceConditions: List<LicenceCondition> = listOf(LicenceConditionGenerator.DEFAULT.withManager())
    ) {
        whenever(
            licenceConditionRepository.findAllByDisposalIdAndMainCategoryCodeNotAndTerminationReasonIsNull(
                disposalId
            )
        ).thenReturn(licenceConditions)
    }

    private fun withReferenceData(vararg referenceData: ReferenceData?) {
        referenceData.forEach {
            whenever(referenceDataRepository.findByCodeAndSetName(it!!.code, it.set.name)).thenReturn(it)
        }
    }
}
