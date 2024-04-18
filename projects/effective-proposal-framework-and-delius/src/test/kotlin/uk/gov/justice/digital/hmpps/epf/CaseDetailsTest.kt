package uk.gov.justice.digital.hmpps.epf

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate

internal class CaseDetailsTest {
    @ParameterizedTest
    @MethodSource("ageAtRelease")
    fun `correctly calculates age at release`(details: CaseDetails, ageAtRelease: Long?) {
        assertThat(details.ageAtRelease, equalTo(ageAtRelease))
    }

    companion object {
        private val details = CaseDetails(
            "A1234BC",
            Name("John", "", "Smith"),
            LocalDate.of(1982, 8, 3),
            "Male",
            Appearance(LocalDate.now(), Court("NA Court")),
            Sentence(LocalDate.of(2022, 8, 2)),
            Provider("N00", "London"),
            null
        )

        private fun CaseDetails.withReleaseDate(releaseDate: LocalDate) =
            copy(sentence = this.sentence?.copy(expectedReleaseDate = releaseDate))

        @JvmStatic
        fun ageAtRelease() = listOf(
            Arguments.of(details, 39L),
            Arguments.of(details.withReleaseDate(LocalDate.of(2022, 8, 3)), 40L),
            Arguments.of(details.withReleaseDate(LocalDate.of(2022, 8, 4)), 40L),
            Arguments.of(details.copy(sentence = null), null)
        )
    }
}
