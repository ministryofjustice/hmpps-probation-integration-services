package uk.gov.justice.digital.hmpps.api.controller

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.service.CaseSummaryService

@ExtendWith(MockitoExtension::class)
internal class CaseSummaryControllerTest {
    @Mock
    lateinit var caseSummaryService: CaseSummaryService

    @InjectMocks
    lateinit var caseSummaryController: CaseSummaryController

    @Test
    fun `get personal details`() {
        caseSummaryController.getPersonalDetails("TEST")
        verify(caseSummaryService).getPersonalDetails("TEST")
    }

    @Test
    fun `get overview`() {
        caseSummaryController.getOverview("TEST")
        verify(caseSummaryService).getOverview("TEST")
    }
}
