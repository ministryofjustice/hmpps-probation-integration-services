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
import uk.gov.justice.digital.hmpps.integrations.prison.SentenceDetail
import java.time.LocalDate

internal class KeyDateCalculatorTest {

    private val calculator = KeyDateCalculator()

    @ParameterizedTest
    @MethodSource("suspensionDateCases")
    fun `check two-thirds point`(
        conditionalReleaseDate: LocalDate?, sentenceExpiryDate: LocalDate?, expected: LocalDate?
    ) {
        val custody = generateCustodialSentence(
            disposal = generateDisposal(generateEvent()), bookingRef = "ABC"
        )
        val result = calculator.suspensionDateIfReset(
            SentenceDetail(
                conditionalReleaseDate = conditionalReleaseDate, sentenceExpiryDate = sentenceExpiryDate
            ), custody
        )
        assertThat(result, equalTo(expected))
    }

    @ParameterizedTest
    @MethodSource("emedCases")
    fun `calculate presumptive em end date`(
        sentenceExpiryDate: LocalDate?, sentenceLength: Long, sdsPlus: Boolean, expected: LocalDate?
    ) {
        val result = calculator.presumptiveElectronicMonitoringEndDate(
            SentenceDetail(sentenceExpiryDate = sentenceExpiryDate),
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
        val result = calculator.presumptiveElectronicMonitoringEndDate(
            SentenceDetail(sentenceExpiryDate = LocalDate.of(2025, 1, 1)),
            OperativeSentenceEnvelope(
                bookingId = 1L,
                containsAnSDSPlusSentence = null,
                sentenceEnvelopeLengthInDays = 50L
            )
        )
        assertThat(result, equalTo(LocalDate.of(2024, 12, 2)))
    }

    @ParameterizedTest
    @MethodSource("finalThirdCases")
    fun `calculate final third date`(sentenceExpiryDate: LocalDate?, sentenceLength: Long, expected: LocalDate?) {
        val result = calculator.finalThirdDate(
            SentenceDetail(
                sentenceExpiryDate = sentenceExpiryDate
            ),
            OperativeSentenceEnvelope(
                bookingId = 1L,
                containsAnSDSPlusSentence = false,
                sentenceEnvelopeLengthInDays = sentenceLength
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
            arguments(LocalDate.of(2025, 1, 1), 1L, false, LocalDate.of(2024, 12, 31)),
            arguments(LocalDate.of(2025, 1, 1), 2L, false, LocalDate.of(2024, 12, 30)),
            arguments(LocalDate.of(2025, 1, 1), 10L, false, LocalDate.of(2024, 12, 26)),
            arguments(LocalDate.of(2025, 1, 1), 12L, false, LocalDate.of(2024, 12, 24)),
            arguments(LocalDate.of(2025, 1, 1), 365L, false, LocalDate.of(2024, 5, 27)),
            arguments(LocalDate.of(2025, 1, 1), 730L, false, LocalDate.of(2023, 10, 21)),

            // SDS+
            arguments(null, 10L, true, null),
            arguments(LocalDate.of(2025, 1, 1), 1L, true, LocalDate.of(2024, 12, 31)),
            arguments(LocalDate.of(2025, 1, 1), 2L, true, LocalDate.of(2024, 12, 31)),
            arguments(LocalDate.of(2025, 1, 1), 50L, true, LocalDate.of(2024, 12, 15)),
            arguments(LocalDate.of(2025, 1, 1), 365L, true, LocalDate.of(2024, 9, 1)),
            arguments(LocalDate.of(2025, 1, 1), 1000L, true, LocalDate.of(2024, 2, 2))
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
    }
}