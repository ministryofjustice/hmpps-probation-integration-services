package uk.gov.justice.digital.hmpps.job

import io.sentry.Sentry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mockStatic
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.context.ApplicationContext
import uk.gov.justice.digital.hmpps.job.CronJobHelper.runThenExit

@ExtendWith(MockitoExtension::class)
class CronJobHelperTest {
    @Mock
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun `captures exception and exits with code 1 on error`() {
        mockStatic(Sentry::class.java).use { sentry ->
            val exception = RuntimeException("error")
            val exitMock = mock<(code: Int) -> Unit>()

            applicationContext.runThenExit(exit = exitMock) { throw exception }

            sentry.verify { Sentry.captureException(exception) }
            verify(exitMock).invoke(1)
        }
    }

    @Test
    fun `runs successfully`() {
        mockStatic(Sentry::class.java).use { sentry ->
            val exitMock = mock<(code: Int) -> Unit>()

            applicationContext.runThenExit(exit = exitMock) { }

            sentry.verify({ Sentry.captureException(any()) }, never())
            verify(exitMock).invoke(0)
        }
    }
}
