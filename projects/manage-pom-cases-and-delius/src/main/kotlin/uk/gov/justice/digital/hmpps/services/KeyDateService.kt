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
    fun mergeHandoverDates(
        personId: Long,
        date: LocalDate?,
        startDate: LocalDate?,
        dryRun: Boolean = false
    ): KeyDateMergeResult {
        val custodyList = custodyRepository.findAllByDisposalEventPersonId(personId)
        val custody = when (custodyList.size) {
            0 -> throw IgnorableMessageException("NoActiveCustodialSentence")
            1 -> custodyList.first()
            else -> throw IgnorableMessageException("MultipleActiveCustodialSentences")
        }

        val existing = keyDateRepository.findHandoverDates(custody.id).associateBy { it.type.code }
        val hod = update(custody.id, HANDOVER_DATE, existing[HANDOVER_DATE.value], date, dryRun)
        val hsd = update(custody.id, HANDOVER_START_DATE, existing[HANDOVER_START_DATE.value], startDate, dryRun)
        return listOf(hod, hsd).maxByOrNull { it.ordinal }!!
    }

    private fun update(
        custodyId: Long,
        typeCode: KeyDate.TypeCode,
        existing: KeyDate?,
        new: LocalDate?,
        dryRun: Boolean = false
    ): KeyDateMergeResult = if (dryRun) dryRunUpdate(existing, new) else {
        if (new != null) {
            keyDateRepository.save(existing?.apply { this.date = new } ?: keyDate(custodyId, typeCode, new))
            if (existing == null) KeyDateMergeResult.KeyDateCreated else KeyDateMergeResult.KeyDateUpdated
        } else {
            if (existing == null) KeyDateMergeResult.NoKeyDateChange else KeyDateMergeResult.DryRunKeyDateDeleted
        }
    }

    private fun dryRunUpdate(existing: KeyDate?, new: LocalDate?): KeyDateMergeResult = if (new != null) {
        if (existing == null) KeyDateMergeResult.DryRunKeyDateCreated else KeyDateMergeResult.DryRunKeyDateUpdated
    } else {
        if (existing == null) KeyDateMergeResult.DryRunNoKeyDateChange else KeyDateMergeResult.DryRunKeyDateDeleted
    }

    private fun keyDate(custodyId: Long, typeCode: KeyDate.TypeCode, date: LocalDate): KeyDate =
        KeyDate(custodyId, referenceDataRepository.keyDateType(typeCode.value), date)
}

enum class KeyDateMergeResult() {
    NoKeyDateChange, KeyDateDeleted, KeyDateUpdated, KeyDateCreated,
    DryRunNoKeyDateChange, DryRunKeyDateDeleted, DryRunKeyDateUpdated, DryRunKeyDateCreated
}
