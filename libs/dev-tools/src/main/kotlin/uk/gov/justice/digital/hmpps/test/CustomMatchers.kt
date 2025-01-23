package uk.gov.justice.digital.hmpps.test

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.SECONDS
import java.time.temporal.Temporal
import java.time.temporal.TemporalUnit

object CustomMatchers {
    fun isWithin(amount: Long, unit: TemporalUnit?): WithinMatcherBuilder {
        return WithinMatcherBuilder(Duration.of(amount, unit))
    }

    fun isCloseTo(dateTime: Temporal): Matcher<Temporal> = isWithin(1, SECONDS).of(dateTime)

    class WithinMatcherBuilder(private val duration: Duration) {
        fun of(expected: Temporal) = object : TypeSafeMatcher<Temporal>() {
            override fun describeTo(description: Description) {
                description.appendText("a value within <$duration> of <$expected>")
            }

            override fun matchesSafely(actual: Temporal) = Duration.between(expected, actual) < duration
        }
    }

    fun isSameTimeAs(expected: ZonedDateTime) = object : TypeSafeMatcher<ZonedDateTime>() {
        override fun describeTo(description: Description) {
            description.appendText("a value at the same time as <$expected>")
        }

        override fun matchesSafely(actual: ZonedDateTime) =
            expected.withZoneSameInstant(EuropeLondon) == actual.withZoneSameInstant(EuropeLondon)
    }
}
