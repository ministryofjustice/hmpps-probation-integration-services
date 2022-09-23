package uk.gov.justice.digital.hmpps.controller

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.Address
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.Court
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.LocalJusticeArea
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.Name
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.Offence
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.PreSentenceReportContext
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.PreSentenceReportService
import java.text.SimpleDateFormat

@ExtendWith(MockitoExtension::class)
class PSRContextControllerTest {
    @Mock
    private lateinit var preSentenceReportService: PreSentenceReportService

    @InjectMocks
    private lateinit var pSRContextController: PSRContextController

    @Test
    fun `Get pre sentence report context`() {
        whenever(preSentenceReportService.getPreSentenceReportContext("1")).thenReturn(getPreSentenceReportContext())
        val result = pSRContextController.getPreSentenceReportContext("1")
        Assertions.assertNotNull(result)
    }

    @Test
    fun `Get pre sentence report context not found`() {
        whenever(preSentenceReportService.getPreSentenceReportContext("1")).thenThrow(NotFoundException("Not found"))

        val exception: Exception = assertThrows(NotFoundException::class.java) { pSRContextController.getPreSentenceReportContext("1") }
        assertTrue(exception.message!!.contains("Not found"))
    }

    private fun getPreSentenceReportContext(): PreSentenceReportContext {

        val formatter = SimpleDateFormat("dd-MM-yyyy")

        return PreSentenceReportContext(
            "X123123",
            Name("forename", "surename", "middlename"),
            formatter.parse("12-12-2000"),
            "PNC123",
            Address("N", "building name", "123", "StreetName", "Town", "District", "County", "NE1 2SW"),
            Offence("MainOffence"),
            listOf(Offence("other offence")),
            Court("CourtName", LocalJusticeArea("Local justice area"))
        )
    }
}
