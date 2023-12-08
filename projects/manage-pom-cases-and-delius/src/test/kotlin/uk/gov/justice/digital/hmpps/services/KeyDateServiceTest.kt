package uk.gov.justice.digital.hmpps.services

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
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.KeyDateRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.findHandoverDates
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceDataSet
import uk.gov.justice.digital.hmpps.messaging.IgnorableMessageException
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class KeyDateServiceTest {
    @Mock
    internal lateinit var custodyRepository: CustodyRepository

    @Mock
    internal lateinit var keyDateRepository: KeyDateRepository

    @Mock
    internal lateinit var referenceDataRepository: ReferenceDataRepository

    @InjectMocks
    internal lateinit var keyDateService: KeyDateService

    @Test
    fun `exception thrown if no active custody record`() {
        val ex =
            assertThrows<IgnorableMessageException> {
                keyDateService.mergeHandoverDates(9817264, LocalDate.now(), LocalDate.now())
            }
        assertThat(ex.message, equalTo("NoActiveCustodialSentence"))
    }

    @Test
    fun `create handover dates`() {
        val personId = 47L
        val custody = givenCustodyRecord(personId)
        withKeyDateTypes()
        whenever(custodyRepository.findAllByDisposalEventPersonId(personId))
            .thenReturn(listOf(custody))

        val res = keyDateService.mergeHandoverDates(personId, LocalDate.now(), LocalDate.now())
        assertThat(res, equalTo(KeyDateMergeResult.KeyDateCreated))
    }

    @Test
    fun `update handover dates`() {
        val personId = 93L
        val custody = givenCustodyRecord(personId)
        val keyDates = givenKeyDates(custody, LocalDate.now().minusDays(1), LocalDate.now().minusDays(1))
        whenever(custodyRepository.findAllByDisposalEventPersonId(personId))
            .thenReturn(listOf(custody))
        whenever(keyDateRepository.findHandoverDates(custody.id))
            .thenReturn(keyDates)

        val res = keyDateService.mergeHandoverDates(personId, LocalDate.now(), LocalDate.now())
        assertThat(res, equalTo(KeyDateMergeResult.KeyDateUpdated))
    }

    @Test
    fun `delete handover dates`() {
        val personId = 42L
        val custody = givenCustodyRecord(personId)
        val keyDates = givenKeyDates(custody, LocalDate.now(), LocalDate.now())
        whenever(custodyRepository.findAllByDisposalEventPersonId(personId))
            .thenReturn(listOf(custody))
        whenever(keyDateRepository.findHandoverDates(custody.id))
            .thenReturn(keyDates)

        val res = keyDateService.mergeHandoverDates(personId, null, null)
        assertThat(res, equalTo(KeyDateMergeResult.KeyDateDeleted))
    }

    @Test
    fun `no change to handover dates`() {
        val personId = 12L
        val custody = givenCustodyRecord(personId)
        whenever(custodyRepository.findAllByDisposalEventPersonId(personId))
            .thenReturn(listOf(custody))

        val res = keyDateService.mergeHandoverDates(personId, null, null)
        assertThat(res, equalTo(KeyDateMergeResult.NoKeyDateChange))
    }

    private fun withKeyDateTypes() {
        whenever(
            referenceDataRepository.findByCode(
                ReferenceDataGenerator.KEY_DATE_HANDOVER_TYPE.code,
                ReferenceDataSet.Code.KEY_DATE_TYPE.value,
            ),
        ).thenReturn(ReferenceDataGenerator.KEY_DATE_HANDOVER_TYPE)

        whenever(
            referenceDataRepository.findByCode(
                ReferenceDataGenerator.KEY_DATE_HANDOVER_START_DATE_TYPE.code,
                ReferenceDataSet.Code.KEY_DATE_TYPE.value,
            ),
        ).thenReturn(ReferenceDataGenerator.KEY_DATE_HANDOVER_START_DATE_TYPE)
    }

    private fun givenCustodyRecord(personId: Long): Custody {
        val event = EventGenerator.generateEvent(personId)
        val disposal = EventGenerator.generateDisposal(event)
        return EventGenerator.generateCustody(disposal)
    }

    private fun givenKeyDates(
        custody: Custody,
        hod: LocalDate,
        hsd: LocalDate,
    ) = listOf(
        EventGenerator.generateKeyDate(custody, ReferenceDataGenerator.KEY_DATE_HANDOVER_TYPE, hod),
        EventGenerator.generateKeyDate(custody, ReferenceDataGenerator.KEY_DATE_HANDOVER_START_DATE_TYPE, hsd),
    )
}
