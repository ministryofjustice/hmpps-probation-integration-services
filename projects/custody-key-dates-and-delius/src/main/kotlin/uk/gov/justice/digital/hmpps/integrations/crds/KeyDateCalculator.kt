package uk.gov.justice.digital.hmpps.integrations.crds

import org.springframework.stereotype.Component
import java.time.LocalDate
import kotlin.math.roundToLong

@Component
class KeyDateCalculator {

    companion object {
        private const val SDS_PERCENTAGE = 0.17
        private const val SDS_PLUS_PERCENTAGE = 0.17
    }

    /**
     * EMED Calculation = CRD + X% of sentence length
     * For SDS this is 17%
     * Currently SDS+ is also 17% but subject to change.
     */
    fun calculatePresumptiveEMEndDate(crd: LocalDate, sentenceLengthDays: Long, isSdsPlus: Boolean?): LocalDate {

        val percentage = if (isSdsPlus == true) {
            SDS_PLUS_PERCENTAGE
        } else {
            SDS_PERCENTAGE
        }
        val extraDays = (sentenceLengthDays * percentage).roundToLong()
        return crd.plusDays(extraDays)
    }

    /**
     * FTHRD Calculation = SLED - 1/3 sentence length
     */
    fun calculateFinalThirdDate(sled: LocalDate, sentenceLengthDays: Long): LocalDate {
        val deduction = sentenceLengthDays / 3
        return sled.minusDays(deduction)
    }
}
