package uk.gov.justice.digital.hmpps.utils.uk.gov.justice.digital.hmpps.utils

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.utils.SearchHelpers
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SearchHelperTest {

    @Test
    fun `lenient date of births are generated`() {
        val dateOfBirths = SearchHelpers.allLenientDateVariations(toDate("1961-05-17"))
        val expected = listOf(
            toDate("1961-01-17"),
            toDate("1961-02-17"),
            toDate("1961-03-17"),
            toDate("1961-04-17"),
            toDate("1961-06-17"),
            toDate("1961-07-17"),
            toDate("1961-08-17"),
            toDate("1961-09-17"),
            toDate("1961-10-17"),
            toDate("1961-11-17"),
            toDate("1961-12-17"),
            toDate("1961-05-16"),
            toDate("1961-05-18"),
            toDate("1961-05-17")
        )
        assertThat(dateOfBirths, equalTo(expected))
    }

    @Test
    fun `converts a pnc to compatible with NDelius`() {
        assertThat(SearchHelpers.formatPncNumber("63/281261B"), equalTo("63/0281261B"))
        assertThat(SearchHelpers.formatPncNumber("63/1B"), equalTo("63/0000001B"))
        assertThat(SearchHelpers.formatPncNumber("63/1B/1B"), equalTo("63/1B/1B"))
        assertThat(SearchHelpers.formatPncNumber("63281261B"), equalTo("63281261B"))
        assertThat(SearchHelpers.formatPncNumber("63/0000001B"), equalTo("63/0000001B"))
    }

    private fun toDate(dob: String): LocalDate {
        return LocalDate.parse(dob, DateTimeFormatter.ISO_DATE)
    }
}
