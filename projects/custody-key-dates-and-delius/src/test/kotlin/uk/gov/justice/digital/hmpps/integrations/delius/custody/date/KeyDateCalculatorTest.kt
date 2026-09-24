package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateCustodialSentence
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateDisposal
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.generateEvent
import uk.gov.justice.digital.hmpps.integrations.crds.OperativeSentenceEnvelope
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.KeyDateCalculator.electronicMonitoringEndDate
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.KeyDateCalculator.finalThirdDate
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.KeyDateCalculator.suspensionDateIfReset
import uk.gov.justice.digital.hmpps.integrations.prison.SentenceDetail
import java.time.LocalDate

internal class KeyDateCalculatorTest {

    @ParameterizedTest
    @MethodSource("suspensionDateCases")
    fun `check two-thirds point`(
        conditionalReleaseDate: LocalDate?, sentenceExpiryDate: LocalDate?, expected: LocalDate?
    ) {
        val custody = generateCustodialSentence(
            disposal = generateDisposal(generateEvent()), bookingRef = "ABC"
        )
        val result = SentenceDetail(
            conditionalReleaseDate = conditionalReleaseDate, sentenceExpiryDate = sentenceExpiryDate
        ).suspensionDateIfReset(custody)
        assertThat(result, equalTo(expected))
    }

    @ParameterizedTest
    @MethodSource("emedCases")
    fun `calculate em end date from CRD plus rounded down percentage of CRDS sentence length`(
        conditionalReleaseDate: LocalDate?, sentenceLength: Long, sdsPlus: Boolean, expected: LocalDate?
    ) {
        val result = SentenceDetail(
            conditionalReleaseDate = conditionalReleaseDate,
            sentenceExpiryDate = LocalDate.of(2026, 1, 1)
        ).electronicMonitoringEndDate(
            OperativeSentenceEnvelope(
                bookingId = 1L,
                containsAnSDSPlusSentence = sdsPlus,
                sentenceEnvelopeLengthInDays = sentenceLength
            )
        )
        assertThat(result, equalTo(expected))
    }

    @Test
    fun `em end date falls back to regular SDS calculation when sds plus flag is null`() {
        val result = SentenceDetail(
            conditionalReleaseDate = LocalDate.of(2025, 1, 1),
            sentenceExpiryDate = LocalDate.of(2026, 1, 1)
        ).electronicMonitoringEndDate(
            OperativeSentenceEnvelope(
                bookingId = 1L,
                containsAnSDSPlusSentence = null,
                sentenceEnvelopeLengthInDays = 50L
            )
        )
        assertThat(result, equalTo(LocalDate.of(2025, 1, 4)))
    }

    @Test
    fun `CRDS key dates are not calculated without a sentence envelope`() {
        val sentenceDetail = SentenceDetail(
            conditionalReleaseDate = LocalDate.of(2025, 1, 1),
            sentenceExpiryDate = LocalDate.of(2026, 1, 1)
        )

        assertThat(sentenceDetail.electronicMonitoringEndDate(null), equalTo(null))
        assertThat(sentenceDetail.finalThirdDate(null), equalTo(null))
    }

    @ParameterizedTest
    @MethodSource("finalThirdCases")
    fun `calculate final third date`(sentenceExpiryDate: LocalDate?, sentenceLength: Long, expected: LocalDate?) {
        val result = SentenceDetail(sentenceExpiryDate = sentenceExpiryDate).finalThirdDate(
            OperativeSentenceEnvelope(
                bookingId = 1L,
                containsAnSDSPlusSentence = false,
                sentenceEnvelopeLengthInDays = sentenceLength
            )
        )
        assertThat(result, equalTo(expected))
    }

    @ParameterizedTest
    @MethodSource("deliusEmedCases")
    fun `calculate em end date from CRD plus rounded down percentage of Delius sentence length`(
        conditionalReleaseDate: LocalDate?,
        sentenceLengthInDays: Long?,
        sdsPlus: Boolean?,
        expected: LocalDate?
    ) {
        val result = SentenceDetail(conditionalReleaseDate = conditionalReleaseDate).electronicMonitoringEndDate(
            generateCustodialSentence(
                disposal = generateDisposal(
                    generateEvent(),
                    sdsPlus = sdsPlus,
                    lengthInDays = sentenceLengthInDays
                ),
                bookingRef = "ABC"
            )
        )
        assertThat(result, equalTo(expected))
    }

    @ParameterizedTest
    @MethodSource("deliusFinalThirdCases")
    fun `calculate final third date from delius`(
        sentenceEndDate: LocalDate?,
        sentenceLengthInDays: Long?,
        expected: LocalDate?
    ) {
        val result = SentenceDetail(sentenceExpiryDate = sentenceEndDate).finalThirdDate(
            generateCustodialSentence(
                disposal = generateDisposal(
                    generateEvent(),
                    lengthInDays = sentenceLengthInDays
                ),
                bookingRef = "ABC"
            )
        )
        assertThat(result, equalTo(expected))
    }

    companion object {
        @JvmStatic
        fun suspensionDateCases() = listOf(
            arguments(null, LocalDate.of(2025, 1, 1), null),
            arguments(LocalDate.of(2025, 1, 1), null, null),
            arguments(LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 1), null),
            arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1), null),
            arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 1)),
            arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 3), LocalDate.of(2025, 1, 2)),
            arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 4), LocalDate.of(2025, 1, 3)),
            arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 1), LocalDate.of(2025, 9, 1)),
            arguments(LocalDate.of(2028, 2, 29), LocalDate.of(2028, 3, 30), LocalDate.of(2028, 3, 20)),
            arguments(LocalDate.of(2099, 6, 30), LocalDate.of(2120, 2, 29), LocalDate.of(2113, 4, 10)),
        )

        @JvmStatic
        fun emedCases() = listOf(
            // SDS
            arguments(null, 10L, false, null),
            arguments(LocalDate.of(2025, 1, 1), 0L, false, LocalDate.of(2025, 1, 1)),
            arguments(LocalDate.of(2025, 1, 1), 1L, false, LocalDate.of(2025, 1, 1)),
            arguments(LocalDate.of(2025, 1, 1), 2L, false, LocalDate.of(2025, 1, 1)),
            arguments(LocalDate.of(2025, 1, 1), 10L, false, LocalDate.of(2025, 1, 1)),
            arguments(LocalDate.of(2025, 1, 1), 12L, false, LocalDate.of(2025, 1, 1)),
            arguments(LocalDate.of(2025, 1, 1), 14L, false, LocalDate.of(2025, 1, 1)),
            arguments(LocalDate.of(2025, 1, 1), 15L, false, LocalDate.of(2025, 1, 2)),
            arguments(LocalDate.of(2025, 1, 1), 100L, false, LocalDate.of(2025, 1, 8)),
            arguments(LocalDate.of(2025, 1, 1), 365L, false, LocalDate.of(2025, 1, 26)),
            arguments(LocalDate.of(2025, 1, 1), 730L, false, LocalDate.of(2025, 2, 21)),

            // SDS+
            arguments(null, 10L, true, null),
            arguments(LocalDate.of(2025, 1, 1), 0L, true, LocalDate.of(2025, 1, 1)),
            arguments(LocalDate.of(2025, 1, 1), 1L, true, LocalDate.of(2025, 1, 1)),
            arguments(LocalDate.of(2025, 1, 1), 2L, true, LocalDate.of(2025, 1, 1)),
            arguments(LocalDate.of(2025, 1, 1), 5L, true, LocalDate.of(2025, 1, 1)),
            arguments(LocalDate.of(2025, 1, 1), 6L, true, LocalDate.of(2025, 1, 2)),
            arguments(LocalDate.of(2025, 1, 1), 50L, true, LocalDate.of(2025, 1, 9)),
            arguments(LocalDate.of(2025, 1, 1), 100L, true, LocalDate.of(2025, 1, 18)),
            arguments(LocalDate.of(2025, 1, 1), 365L, true, LocalDate.of(2025, 3, 4)),
            arguments(LocalDate.of(2025, 1, 1), 1000L, true, LocalDate.of(2025, 6, 20))
        )

        @JvmStatic
        fun finalThirdCases() = listOf(
            arguments(null, 10L, null),
            arguments(LocalDate.of(2025, 1, 1), 0L, LocalDate.of(2025, 1, 1)),
            arguments(LocalDate.of(2025, 1, 1), 1L, LocalDate.of(2024, 12, 31)),
            arguments(LocalDate.of(2025, 1, 1), 2L, LocalDate.of(2024, 12, 31)),
            arguments(LocalDate.of(2025, 1, 1), 10L, LocalDate.of(2024, 12, 28)),
            arguments(LocalDate.of(2025, 1, 1), 11L, LocalDate.of(2024, 12, 28)),
            arguments(LocalDate.of(2025, 1, 1), 12L, LocalDate.of(2024, 12, 28)),
            arguments(LocalDate.of(2025, 1, 1), 50L, LocalDate.of(2024, 12, 15)),
            arguments(LocalDate.of(2025, 1, 1), 365L, LocalDate.of(2024, 9, 1)),
            arguments(LocalDate.of(2025, 1, 1), 730L, LocalDate.of(2024, 5, 2)),
            arguments(LocalDate.of(2025, 1, 1), 1000L, LocalDate.of(2024, 2, 2))
        )

        @JvmStatic
        fun deliusEmedCases() = emedCases() + listOf(
            arguments(null, 366L, false, null),
            arguments(LocalDate.of(2025, 1, 1), null, false, null),
            arguments(LocalDate.of(2025, 1, 1), null, true, null),
            arguments(LocalDate.of(2025, 1, 1), 366L, false, LocalDate.of(2025, 1, 26)),
            arguments(LocalDate.of(2025, 1, 1), 366L, true, LocalDate.of(2025, 3, 4)),
            arguments(LocalDate.of(2025, 1, 1), 366L, null, LocalDate.of(2025, 1, 26)),
            arguments(LocalDate.of(2027, 3, 12), 820L, false, LocalDate.of(2027, 5, 8))
        )

        @JvmStatic
        fun deliusFinalThirdCases() = listOf(
            arguments(null, 366L, null),
            arguments(LocalDate.of(2025, 1, 1), null, null),
            arguments(LocalDate.of(2025, 1, 1), 366L, LocalDate.of(2024, 9, 1)),
            arguments(LocalDate.of(2025, 1, 1), 9L, LocalDate.of(2024, 12, 29)),
            arguments(LocalDate.of(2027, 3, 12), 820L, LocalDate.of(2026, 6, 11)),
            arguments(LocalDate.of(2042, 10, 23), 7427L, LocalDate.of(2036, 1, 12))
        )
    }
}