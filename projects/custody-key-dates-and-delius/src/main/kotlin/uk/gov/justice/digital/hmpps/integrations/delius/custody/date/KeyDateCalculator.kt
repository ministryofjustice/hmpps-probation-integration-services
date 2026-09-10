package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.crds.OperativeSentenceEnvelope
import uk.gov.justice.digital.hmpps.integrations.prison.SentenceDetail
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

@Component
class KeyDateCalculator {
    /**
     * EMED Calculation
     * For SDS Sentences, EMED = SED - (60% of the total sentence length)
     * For SDS+ Sentences, EMED = SED - (1/3rd of the total sentence length)
     */
    fun presumptiveElectronicMonitoringEndDate(
        sentenceDetail: SentenceDetail, envelope: OperativeSentenceEnvelope
    ): LocalDate? = sentenceDetail.sentenceExpiryDate?.let { sed ->
        val lengthInDays = envelope.sentenceEnvelopeLengthInDays
        val deduction = if (envelope.containsAnSDSPlusSentence == true) {
            ceil(lengthInDays / 3.0).toLong()
        } else {
            ceil((lengthInDays * 60.0) / 100.0).toLong()
        }
        sed.minusDays(deduction)
    }

    /**
     * FTHRD Calculation = SLED - 1/3 sentence length
     */
    fun finalThirdDate(sentenceDetail: SentenceDetail, envelope: OperativeSentenceEnvelope): LocalDate? =
        sentenceDetail.sentenceExpiryDate?.let { sed ->
            val deduction = ceil(envelope.sentenceEnvelopeLengthInDays / 3.0).toLong()
            sed.minusDays(deduction)
        }

    /**
     * Calculate EMED using Delius data
     * sentenceEndDate should be the sentence expiry date (SED), or the disposal's notional end date when SED is missing.
     * For SDS Sentences, EMED = sentenceEndDate - (60% of the sentence length calculated as sentenceEndDate - disposal_date)
     * For SDS+ Sentences, EMED = SED - (1/3rd of the sentence length)
     */
    fun presumptiveElectronicMonitoringEndDateFromDelius(
        sentenceEndDate: LocalDate?,
        disposalDate: LocalDate?,
        sdsPlus: Boolean?
    ): LocalDate? {
        val endDate = sentenceEndDate ?: return null
        val lengthInDays = calculateSentenceLengthInDays(disposalDate, endDate) ?: return null
        val deduction = if (sdsPlus == true) {
            ceil(lengthInDays / 3.0).toLong()
        } else {
            ceil((lengthInDays * 60.0) / 100.0).toLong()
        }
        return endDate.minusDays(deduction)
    }

    /**
     * Calculate Final Third Date using Delius data
     * sentenceEndDate should be the sentence expiry date (SED), or the disposal's notional end date when SED is missing.
     * FTHRD = sentenceEndDate - 1/3 sentence length (calculated as sentenceEndDate - disposal_date)
     */
    fun finalThirdDateFromDelius(
        sentenceEndDate: LocalDate?,
        disposalDate: LocalDate?
    ): LocalDate? {
        val endDate = sentenceEndDate ?: return null
        val lengthInDays = calculateSentenceLengthInDays(disposalDate, endDate) ?: return null
        val deduction = ceil(lengthInDays / 3.0).toLong()
        return endDate.minusDays(deduction)
    }

    /**
     * Reset suspension date = 2/3 between start and end dates
     */
    fun suspensionDateIfReset(sentenceDetail: SentenceDetail, custody: Custody): LocalDate? =
        custody.disposal?.takeIf { it.type.determinateSentence }?.let {
            val startDate = it.event.firstReleaseDate ?: sentenceDetail.conditionalReleaseDate ?: return null
            val endDate = sentenceDetail.sentenceExpiryDate ?: return null
            if (startDate < endDate) {
                val daysBetween = ChronoUnit.DAYS.between(startDate, endDate)
                startDate.plusDays(daysBetween * 2 / 3)
            } else {
                null
            }
        }

    /**
     * Calculate sentence length in days from disposal date to end date.
     * Returns null when either date is missing or disposal is after the end date.
     */
    private fun calculateSentenceLengthInDays(
        disposalDate: LocalDate?,
        endDate: LocalDate?
    ): Long? {
        val start = disposalDate ?: return null
        val end = endDate ?: return null
        if (start.isAfter(end)) return null
        return ChronoUnit.DAYS.between(start, end)
    }
}