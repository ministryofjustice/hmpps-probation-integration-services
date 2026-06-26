package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.integrations.crds.OperativeSentenceEnvelope
import uk.gov.justice.digital.hmpps.integrations.prison.SentenceDetail
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToLong

@Component
class KeyDateCalculator {

    companion object {
        private const val SDS_PERCENTAGE = 0.07
        private const val SDS_PLUS_PERCENTAGE = 0.17
    }

    /**
     * EMED Calculation = CRD + X% of sentence length
     * For SDS this is 7%
     * For SDS+ this is 17%
     */
    fun presumptiveElectronicMonitoringEndDate(
        sentenceDetail: SentenceDetail, envelope: OperativeSentenceEnvelope
    ): LocalDate? = sentenceDetail.conditionalReleaseDate?.let { crd ->
        val percentage = if (envelope.containsAnSDSPlusSentence) {
            SDS_PLUS_PERCENTAGE
        } else {
            SDS_PERCENTAGE
        }
        val extraDays = (envelope.sentenceEnvelopeLengthInDays * percentage).roundToLong()
        crd.plusDays(extraDays)
    }

    /**
     * FTHRD Calculation = SLED - 1/3 sentence length
     */
    fun finalThirdDate(sentenceDetail: SentenceDetail, envelope: OperativeSentenceEnvelope): LocalDate? =
        sentenceDetail.sentenceExpiryDate?.let { sed ->
            val deduction = envelope.sentenceEnvelopeLengthInDays / 3
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