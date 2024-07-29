package uk.gov.justice.digital.hmpps

import okhttp3.internal.wait
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ProxyToNewIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var taskExecutor: ThreadPoolTaskExecutor

    @MockBean
    lateinit var featureFlags: FeatureFlags

    @MockBean
    lateinit var telemetryService: TelemetryService

    @BeforeEach
    fun setup() {
        whenever(featureFlags.enabled("ccd-offender-detail-enabled")).thenReturn(true)
        whenever(featureFlags.enabled("ccd-offender-summary-enabled")).thenReturn(true)
    }

    @Test
    fun `proxies to community api for offenders all`() {

        mockMvc
            .perform(get("/secure/offenders/crn/C123456/all").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.otherIds.crn").value("C123456"))

        whileTrue { taskExecutor.threadPoolExecutor.completedTaskCount == 0L }
        verify(telemetryService).trackEvent(eq("ComparisonFailureEvent"), any(), any())

    }

    @Test
    fun `proxies to community api for offenders summary`() {
        mockMvc
            .perform(get("/secure/offenders/crn/C123456").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.contactDetails.emailAddresses[0]").value("test@test.none"))
    }

    private fun whileTrue(action: () -> Boolean) {
        while(action.invoke());
    }
}
