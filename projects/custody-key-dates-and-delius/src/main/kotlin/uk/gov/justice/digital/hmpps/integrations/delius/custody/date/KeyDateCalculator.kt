package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.crds.OperativeSentenceEnvelope
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyDateType.AUTOMATIC_CONDITIONAL_RELEASE_DATE
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyDateType.SENTENCE_EXPIRY_DATE
import uk.gov.justice.digital.hmpps.integrations.prison.SentenceDetail
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.floor

@Component
object KeyDateCalculator {
    /**
     * EMED Calculation
     * For SDS Sentences, EMED = CRD + 7% of sentence length in days.
     * For SDS+ Sentences, EMED = CRD + 17% of sentence length in days.
     */
    fun SentenceDetail.electronicMonitoringEndDate(envelope: OperativeSentenceEnvelope?): LocalDate? {
        if (envelope == null) return null

        val factor = if (envelope.containsAnSDSPlusSentence == true) 0.17 else 0.07
        return conditionalReleaseDate?.plusDays(floor(envelope.sentenceEnvelopeLengthInDays * factor).toLong())
    }

    /**
     * FTHRD Calculation = SLED - 1/3 sentence length
     */
    fun SentenceDetail.finalThirdDate(envelope: OperativeSentenceEnvelope?): LocalDate? {
        if (envelope == null || envelope.containsAnSDSPlusSentence == true) return null

        val deduction = ceil(envelope.sentenceEnvelopeLengthInDays / 3.0).toLong()
        return sentenceExpiryDate?.minusDays(deduction)
    }

    /**
     * Calculate EMED using Delius data
     * sentenceEndDate should be the sentence expiry date (SED), or the disposal's notional end date when SED is missing.
     * For SDS Sentences, EMED = CRD + 7% of sentence length in days.
     * For SDS+ Sentences, EMED = CRD + 17% of sentence length in days.
     */
    fun SentenceDetail.electronicMonitoringEndDate(custody: Custody): LocalDate? {
        if (!custody.disposal.type.determinateCustody || custody.disposal.lengthInDays == null) return null

        val factor = if (custody.disposal.sdsPlus == true) 0.17 else 0.07
        val conditionalReleaseDate = conditionalReleaseDate
            ?: custody.keyDates.firstOrNull { it.type.code == AUTOMATIC_CONDITIONAL_RELEASE_DATE.code }?.date
        return conditionalReleaseDate?.plusDays(floor(custody.disposal.lengthInDays * factor).toLong())
    }

    /**
     * Calculate Final Third Date using Delius data
     * sentenceEndDate should be the sentence expiry date (SED), or the disposal's notional end date when SED is missing.
     * FTHRD = sentenceEndDate - (1/3rd of disposal length in days).
     */
    fun SentenceDetail.finalThirdDate(custody: Custody): LocalDate? {
        if (!custody.disposal.type.determinateCustody || custody.disposal.lengthInDays == null || custody.disposal.sdsPlus == true) return null

        val endDate = sentenceExpiryDate
            ?: custody.keyDates.firstOrNull { it.type.code == SENTENCE_EXPIRY_DATE.code }?.date
            ?: custody.disposal.notionalEndDate
        val deduction = ceil(custody.disposal.lengthInDays / 3.0).toLong()
        return endDate?.minusDays(deduction)
    }

    /**
     * Reset suspension date = 2/3 between start and end dates
     */
    fun SentenceDetail.suspensionDateIfReset(custody: Custody): LocalDate? =
        custody.disposal.takeIf { it.type.determinateSentence }?.event?.let { event ->
            val startDate = event.firstReleaseDate
                ?: conditionalReleaseDate
                ?: custody.keyDates.firstOrNull { it.type.code == AUTOMATIC_CONDITIONAL_RELEASE_DATE.code }?.date
                ?: return null
            val endDate = sentenceExpiryDate
                ?: custody.keyDates.firstOrNull { it.type.code == SENTENCE_EXPIRY_DATE.code }?.date
                ?: return null
            if (startDate < endDate) {
                val daysBetween = ChronoUnit.DAYS.between(startDate, endDate)
                startDate.plusDays(daysBetween * 2 / 3)
            } else null
        }

    /**
     * Only apply PSS end date for sentences with a requirement for PSS
     */
    fun SentenceDetail.pssEndDateIfPss(custody: Custody): LocalDate? =
        postSentenceSupervisionEndDate.takeIf { custody.disposal.type.pssRequirement == true }
}