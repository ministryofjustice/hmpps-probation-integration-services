package uk.gov.justice.digital.hmpps.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.KeyDate
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.KeyDate.TypeCode.HANDOVER_DATE
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.KeyDate.TypeCode.HANDOVER_START_DATE
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.KeyDateRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.findHandoverDates
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.keyDateType
import uk.gov.justice.digital.hmpps.messaging.IgnorableMessageException
import java.time.LocalDate

@Transactional
@Service
class KeyDateService(
    private val custodyRepository: CustodyRepository,
    private val keyDateRepository: KeyDateRepository,
    private val referenceDataRepository: ReferenceDataRepository
) {
    @Transactional
    fun mergeHandoverDates(personId: Long, date: LocalDate, startDate: LocalDate?): KeyDateMergeResult {
        val custodyList = custodyRepository.findAllByDisposalEventPersonId(personId)
        val custody = when (custodyList.size) {
            0 -> throw IgnorableMessageException("NoActiveCustodialSentence")
            1 -> custodyList.first()
            else -> throw IgnorableMessageException("MultipleActiveCustodialSentences")
        }

        val existing = keyDateRepository.findHandoverDates(custody.id).associateBy { it.type.code }
        val handoverDate = existing[HANDOVER_DATE.value]?.apply { this.date = date }
            ?: keyDate(custody.id, HANDOVER_DATE, date)
        val handoverStartDate = startDate?.let {
            existing[HANDOVER_START_DATE.value]?.apply { this.date = it }
                ?: keyDate(custody.id, HANDOVER_START_DATE, it)
        }

        val saved = keyDateRepository.saveAll(listOfNotNull(handoverDate, handoverStartDate))
        return if (saved.size > existing.size) KeyDateMergeResult.KEY_DATE_CREATED else KeyDateMergeResult.KEY_DATE_UPDATED
    }

    private fun keyDate(custodyId: Long, typeCode: KeyDate.TypeCode, date: LocalDate): KeyDate =
        KeyDate(custodyId, referenceDataRepository.keyDateType(typeCode.value), date, null)
}

enum class KeyDateMergeResult {
    KEY_DATE_CREATED, KEY_DATE_UPDATED
}
