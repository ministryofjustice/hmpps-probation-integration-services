package uk.gov.justice.digital.hmpps.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.KeyDate
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.KeyDate.TypeCode.HANDOVER_DATE
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.KeyDate.TypeCode.HANDOVER_START_DATE
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.KeyDateRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.findHandoverDates
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.keyDateType
import java.time.LocalDate

@Transactional
@Service
class KeyDateService(
    private val custodyRepository: CustodyRepository,
    private val keyDateRepository: KeyDateRepository,
    private val referenceDataRepository: ReferenceDataRepository
) {
    @Transactional
    fun mergeHandoverDates(personId: Long, date: LocalDate?, startDate: LocalDate?, dryRun: Boolean = false): KeyDateMergeResult {
        val custodyList = custodyRepository.findAllByDisposalEventPersonId(personId)
        val custody = when (custodyList.size) {
            0 -> throw IgnorableMessageException("NoActiveCustodialSentence")
            1 -> custodyList.first()
            else -> throw IgnorableMessageException("MultipleActiveCustodialSentences")
        }

        val existing = keyDateRepository.findHandoverDates(custody.id).associateBy { it.type.code }
        val hod = update(custody.id, HANDOVER_DATE, existing[HANDOVER_DATE.value], date, dryRun)
        val hsd = update(custody.id, HANDOVER_START_DATE, existing[HANDOVER_START_DATE.value], startDate)
        return listOf(hod, hsd).maxByOrNull { it.ordinal }!!
    }

    private fun update(
        custodyId: Long,
        typeCode: KeyDate.TypeCode,
        keyDate: KeyDate?,
        date: LocalDate?,
        dryRun: Boolean = false
    ): KeyDateMergeResult {
        date?.let {
            return when (dryRun) {
                false -> {
                    keyDateRepository.save(keyDate?.apply { this.date = date } ?: keyDate(custodyId, typeCode, it))
                     if (keyDate == null) KeyDateMergeResult.KeyDateCreated else KeyDateMergeResult.KeyDateUpdated
                }

                else -> if (keyDate == null) KeyDateMergeResult.DryRunKeyDateCreated else KeyDateMergeResult.DryRunKeyDateUpdated
            }
        }
        return if (!dryRun) KeyDateMergeResult.NoKeyDateChange else KeyDateMergeResult.DryRunNoKeyDateChange
    }

    private fun keyDate(custodyId: Long, typeCode: KeyDate.TypeCode, date: LocalDate): KeyDate =
        KeyDate(custodyId, referenceDataRepository.keyDateType(typeCode.value), date, null)
}

enum class KeyDateMergeResult {
    NoKeyDateChange, KeyDateUpdated, KeyDateCreated, DryRunNoKeyDateChange, DryRunKeyDateUpdated, DryRunKeyDateCreated
}
