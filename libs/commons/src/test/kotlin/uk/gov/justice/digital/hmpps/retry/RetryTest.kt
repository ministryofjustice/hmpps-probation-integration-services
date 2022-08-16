package uk.gov.justice.digital.hmpps.retry

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.rmi.UnexpectedException
import java.util.concurrent.atomic.AtomicInteger

internal class RetryTest {

    @Test
    fun `when exception thrown retry until max retries`() {
        val counter = AtomicInteger(0)
        assertThrows<UnexpectedException> {
            retry(3) {
                counter.incrementAndGet()
                throw UnexpectedException("Unexpected Exception")
            }
        }
        assertThat(counter.get(), equalTo(3))
    }

    @Test
    fun `when successful no retries`() {
        val counter = AtomicInteger(0)
        val result = retry(3) {
            counter.incrementAndGet()
        }
        assertThat(result, equalTo(1))
    }
}
