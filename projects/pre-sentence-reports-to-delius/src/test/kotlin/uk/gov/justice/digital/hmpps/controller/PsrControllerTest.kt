package uk.gov.justice.digital.hmpps.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.PreSentenceReportService
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class PsrControllerTest {

    @Mock
    lateinit var psrService: PreSentenceReportService

    @Mock
    lateinit var authentication: Authentication

    lateinit var psrController: PsrController

    @BeforeEach
    fun setUp() {
        psrController = PsrController("https://psr-service.gov.uk", psrService)
    }

    @Test
    fun `get pre-sentence report url`() {
        val uuid = UUID.randomUUID()
        val urn = "urn:uk:gov:hmpps:pre-sentence-service:report:$uuid"
        whenever(psrService.getPreSentenceReportUrl(uuid)).thenReturn("/pre-sentence-report/001")

        val res = psrController.findUrl("P123456", urn, authentication)

        assertThat(res.headers["Location"]).endsWith("/pre-sentence-report/001")
    }

    @Test
    fun `get new-tech report url`() {
        val uuid = UUID.randomUUID()
        val urn = "urn:uk:gov:hmpps:alfresco:document:$uuid"
        val username = "john.smith"
        whenever(authentication.name).thenReturn(username)
        whenever(psrService.getLegacyNewTechReportUrl(uuid, username)).thenReturn("/new-tech/002")

        val res = psrController.findUrl("L123456", urn, authentication)

        assertThat(res.headers["Location"]).endsWith("/new-tech/002")
    }
}
