package uk.gov.justice.digital.hmpps.integrations.prison

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate

internal class SuspensionDateTest {
    @ParameterizedTest
    @MethodSource("testCases")
    fun `check two-thirds point`(
        conditionalReleaseDate: LocalDate?,
        sentenceExpiryDate: LocalDate?,
        expected: LocalDate?
    ) {
        val suspensionDateIfReset = SentenceDetail(
            conditionalReleaseDate = conditionalReleaseDate,
            sentenceExpiryDate = sentenceExpiryDate
        ).suspensionDateIfReset

        assertThat(suspensionDateIfReset, equalTo(expected))
    }

    companion object {
        @JvmStatic
        private fun testCases() = listOf(
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
    }
}