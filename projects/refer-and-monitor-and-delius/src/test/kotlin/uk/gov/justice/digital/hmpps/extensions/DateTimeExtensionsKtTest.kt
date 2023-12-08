package uk.gov.justice.digital.hmpps.extensions

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.ZonedDateTime

internal class DateTimeExtensionsKtTest {
    @ParameterizedTest
    @MethodSource("dateChangedParams")
    fun `correctly detects date changed`(
        first: ZonedDateTime?,
        second: ZonedDateTime?,
        changed: Boolean,
    ) {
        assertThat(first.hasChanged(second), equalTo(changed))
    }

    companion object {
        private val now = ZonedDateTime.now()

        @JvmStatic
        fun dateChangedParams() =
            listOf(
                Arguments.of(null, now, true),
                Arguments.of(now, null, true),
                Arguments.of(null, null, false),
                Arguments.of(now, now, false),
                Arguments.of(now, now.plusSeconds(10), true),
            )
    }
}
