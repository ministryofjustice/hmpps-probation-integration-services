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
        val deduction = if (envelope.containsAnSDSPlusSentence) {
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
}