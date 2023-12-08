package uk.gov.justice.digital.hmpps.test

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import java.time.Duration
import java.time.temporal.ChronoUnit.SECONDS
import java.time.temporal.Temporal
import java.time.temporal.TemporalUnit

object CustomMatchers {
    fun isWithin(
        amount: Long,
        unit: TemporalUnit?,
    ): WithinMatcherBuilder {
        return WithinMatcherBuilder(Duration.of(amount, unit))
    }

    fun isCloseTo(dateTime: Temporal): Matcher<Temporal> = isWithin(1, SECONDS).of(dateTime)

    class WithinMatcherBuilder(private val duration: Duration) {
        fun of(expected: Temporal) =
            object : TypeSafeMatcher<Temporal>() {
                override fun describeTo(description: Description) {
                    description.appendText("a value within <$duration> of <$expected>")
                }

                override fun matchesSafely(actual: Temporal) = Duration.between(expected, actual) < duration
            }
    }
}
