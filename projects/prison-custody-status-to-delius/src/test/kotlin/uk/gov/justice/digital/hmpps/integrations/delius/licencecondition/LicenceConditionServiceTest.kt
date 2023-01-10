package uk.gov.justice.digital.hmpps.integrations.delius.licencecondition

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionTransferGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataSetGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.manager.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.transfer.LicenceConditionTransfer
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.transfer.LicenceConditionTransferRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.TransferStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.transfer.RejectedTransferDiaryRepository
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class LicenceConditionServiceTest {
    @Mock lateinit var licenceConditionRepository: LicenceConditionRepository
    @Mock lateinit var orderManagerRepository: OrderManagerRepository
    @Mock lateinit var contactRepository: ContactRepository
    @Mock lateinit var contactTypeRepository: ContactTypeRepository
    @Mock lateinit var licenceConditionTransferRepository: LicenceConditionTransferRepository
    @Mock lateinit var referenceDataRepository: ReferenceDataRepository
    @Mock lateinit var rejectedTransferDiaryRepository: RejectedTransferDiaryRepository
    @InjectMocks lateinit var licenceConditionService: LicenceConditionService

    @Test
    fun nothingIsDoneWhenThereAreNoLicenceConditions() {
        licenceConditionService.terminateLicenceConditionsForDisposal(1L, ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON, ZonedDateTime.now())
        verify(licenceConditionRepository, never()).save(any())
    }

    @Test
    fun missingTransferStatusIsThrown() {
        withLicenceConditions()
        val exception = assertThrows<NotFoundException> {
            licenceConditionService.terminateLicenceConditionsForDisposal(1L, ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON, ZonedDateTime.now())
        }
        assertEquals("TRANSFER STATUS with code of TR not found", exception.message)
    }

    @Test
    fun missingRejectedDecisionIsThrown() {
        withLicenceConditions()
        withReferenceData(
            ReferenceDataGenerator.TRANSFER_STATUS[TransferStatusCode.REJECTED]
        )

        val exception = assertThrows<NotFoundException> {
            licenceConditionService.terminateLicenceConditionsForDisposal(1L, ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON, ZonedDateTime.now())
        }
        assertEquals("ACCEPTED DECISION with code of R not found", exception.message)
    }

    @Test
    fun missingRejectionReasonIsThrown() {
        withLicenceConditions()
        withReferenceData(
            ReferenceDataGenerator.TRANSFER_STATUS[TransferStatusCode.REJECTED],
            ReferenceDataGenerator.generate("R", ReferenceDataSetGenerator.generate("ACCEPTED DECISION")),
        )

        val exception = assertThrows<NotFoundException> {
            licenceConditionService.terminateLicenceConditionsForDisposal(1L, ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON, ZonedDateTime.now())
        }
        assertEquals("LICENCE AREA TRANSFER REJECTION REASON with code of TWR not found", exception.message)
    }

    @Test
    fun missingOrderManagerIsThrown() {
        withLicenceConditions()
        withReferenceData(
            ReferenceDataGenerator.TRANSFER_STATUS[TransferStatusCode.REJECTED],
            ReferenceDataGenerator.generate("R", ReferenceDataSetGenerator.generate("ACCEPTED DECISION")),
            ReferenceDataGenerator.generate("TWR", ReferenceDataSetGenerator.generate("LICENCE AREA TRANSFER REJECTION REASON")),
        )

        val exception = assertThrows<NotFoundException> {
            licenceConditionService.terminateLicenceConditionsForDisposal(1L, ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON, ZonedDateTime.now())
        }
        assertThat(exception.message, matchesPattern("OrderManager with eventId of \\d* not found"))
    }

    @Test
    fun missingContactTypeIsThrown() {
        val event = EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT)
        withLicenceConditions(event)
        withReferenceData(
            ReferenceDataGenerator.TRANSFER_STATUS[TransferStatusCode.REJECTED],
            ReferenceDataGenerator.generate("R", ReferenceDataSetGenerator.generate("ACCEPTED DECISION")),
            ReferenceDataGenerator.generate("TWR", ReferenceDataSetGenerator.generate("LICENCE AREA TRANSFER REJECTION REASON")),
        )
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(OrderManagerGenerator.generate(event))

        val exception = assertThrows<NotFoundException> {
            licenceConditionService.terminateLicenceConditionsForDisposal(event.disposal!!.id, ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON, ZonedDateTime.now())
        }
        assertEquals("ContactType with code of ETER not found", exception.message)
    }

    @Test
    fun licenceConditionsAreTerminated() {
        val now = ZonedDateTime.now()
        val event = EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT)
        val licenceConditions = List(3) { LicenceConditionGenerator.generate(event) }
        withLicenceConditions(event, licenceConditions)
        withReferenceData(
            ReferenceDataGenerator.TRANSFER_STATUS[TransferStatusCode.REJECTED],
            ReferenceDataGenerator.generate("R", ReferenceDataSetGenerator.generate("ACCEPTED DECISION")),
            ReferenceDataGenerator.generate("TWR", ReferenceDataSetGenerator.generate("LICENCE AREA TRANSFER REJECTION REASON")),
        )
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(OrderManagerGenerator.generate(event))
        whenever(contactTypeRepository.findByCode(ContactTypeCode.COMPONENT_TERMINATED.code)).thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.COMPONENT_TERMINATED])

        licenceConditionService.terminateLicenceConditionsForDisposal(event.disposal!!.id, ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON, now)

        licenceConditions.forEach {
            assertEquals(it.terminationDate, now)
            assertEquals(it.terminationReason, ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON)
            assertFalse(it.pendingTransfer!!)
            verify(licenceConditionRepository).save(it)
            verify(contactRepository).save(check { contact -> assertThat(contact.licenceConditionId, equalTo(it.id)) })
        }
    }

    @Test
    fun pendingTransfersAreTerminated() {
        val now = ZonedDateTime.now()
        val event = EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT)
        val licenceCondition = LicenceConditionGenerator.generate(event)
        val licenceConditionTransfers = List(3) { LicenceConditionTransferGenerator.generate(licenceCondition) }
        withLicenceConditions(event, listOf(licenceCondition))
        withLicenceConditionTransfers(licenceCondition, licenceConditionTransfers)
        withReferenceData(
            ReferenceDataGenerator.TRANSFER_STATUS[TransferStatusCode.REJECTED],
            ReferenceDataGenerator.generate("R", ReferenceDataSetGenerator.generate("ACCEPTED DECISION")),
            ReferenceDataGenerator.generate("TWR", ReferenceDataSetGenerator.generate("LICENCE AREA TRANSFER REJECTION REASON")),
        )
        whenever(orderManagerRepository.findByEventId(event.id)).thenReturn(OrderManagerGenerator.generate(event))
        whenever(contactTypeRepository.findByCode(ContactTypeCode.COMPONENT_TERMINATED.code)).thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.COMPONENT_TERMINATED])
        whenever(contactTypeRepository.findByCode(ContactTypeCode.COMPONENT_PROVIDER_TRANSFER_REJECTED.code)).thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.COMPONENT_PROVIDER_TRANSFER_REJECTED])

        licenceConditionService.terminateLicenceConditionsForDisposal(event.disposal!!.id, ReferenceDataGenerator.LICENCE_CONDITION_TERMINATION_REASON, now)

        licenceConditionTransfers.forEach {
            assertEquals(it.status, ReferenceDataGenerator.TRANSFER_STATUS[TransferStatusCode.REJECTED])
            assertEquals(it.statusDate, now)
        }
        verify(licenceConditionTransferRepository).saveAll(licenceConditionTransfers)
    }

    private fun withLicenceConditionTransfers(
        licenceCondition: LicenceCondition,
        transfers: List<LicenceConditionTransfer> = List(3) { LicenceConditionTransferGenerator.generate(licenceCondition) }
    ) {
        whenever(licenceConditionTransferRepository.findAllByLicenceConditionIdAndStatusCode(licenceCondition.id, TransferStatusCode.PENDING.code))
            .thenReturn(transfers)
    }

    private fun withLicenceConditions(
        event: Event,
        licenceConditions: List<LicenceCondition> = List(3) { LicenceConditionGenerator.generate(event) },
    ) {
        withLicenceConditions(event.disposal!!.id, licenceConditions)
    }

    private fun withLicenceConditions(
        disposalId: Long = 1L,
        licenceConditions: List<LicenceCondition> = listOf(LicenceConditionGenerator.DEFAULT)
    ) {
        whenever(licenceConditionRepository.findAllByDisposalIdAndMainCategoryCodeNotAndTerminationReasonIsNull(disposalId))
            .thenReturn(licenceConditions)
    }

    private fun withReferenceData(vararg referenceData: ReferenceData?) {
        referenceData.forEach {
            whenever(referenceDataRepository.findByCodeAndSetName(it!!.code, it.set.name)).thenReturn(it)
        }
    }
}
