package uk.gov.justice.digital.hmpps.datetime

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class DateTimeFormatterTest {
    @Test
    fun `formatter produces expected output`() {
        val datetime = ZonedDateTime.of(2022, 8, 1, 12, 30, 30, 21, EuropeLondon)
        val output = DeliusDateTimeFormatter.format(datetime)
        assertThat(output, equalTo("2022/08/01 12:30:30"))
    }
}
