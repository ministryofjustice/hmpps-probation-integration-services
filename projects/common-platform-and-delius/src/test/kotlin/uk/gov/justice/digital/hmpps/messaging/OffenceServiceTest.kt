package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import uk.gov.justice.digital.hmpps.integrations.delius.entity.DetailedOffence
import uk.gov.justice.digital.hmpps.integrations.delius.entity.DetailedOffenceRepository
import uk.gov.justice.digital.hmpps.service.OffenceService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.io.ByteArrayInputStream
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class OffenceServiceTest {

    @Mock
    lateinit var detailedOffenceRepository: DetailedOffenceRepository

    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var s3Client: S3Client

    private val bucketName = "bucket-name"

    private lateinit var offenceService: OffenceService

    @BeforeEach
    fun setUp() {
        offenceService = OffenceService(detailedOffenceRepository, telemetryService, s3Client, bucketName)
    }

    @Test
    fun `csv is parsed into priority map`() {
        mockS3Client()

        val priorityMap = offenceService.priorityMap

        assertThat(priorityMap, equalTo(mapOf("00100" to 60, "00101" to 20, "00102" to 30)))
    }

    @Test
    fun `Main offence is set to the offence with the lowest priority`() {
        mockS3Client()

        whenever(detailedOffenceRepository.findByCode("TN42001")).thenReturn(
            DetailedOffence(
                code = "TN42001",
                description = "Offence 1",
                startDate = LocalDate.now(),
                endDate = null,
                homeOfficeCode = "001/00",
                homeOfficeDescription = "Offence 1",
                legislation = null,
                category = null
            )
        )
        whenever(detailedOffenceRepository.findByCode("ZZ00120")).thenReturn(
            DetailedOffence(
                code = "ZZ00120",
                description = "Offence 2",
                startDate = LocalDate.now(),
                endDate = null,
                homeOfficeCode = "001/01",
                homeOfficeDescription = "Offence 2",
                legislation = null,
                category = null
            )
        )

        val result = offenceService.findMainOffence(listOf(
            HearingOffence(
                id = "0",
                offenceTitle = "Offence 1",
                offenceCode = "TN42001",
                wording = "Offence 1",
                offenceLegislation = "Offence 1",
                listingNumber = 0
            ),
            HearingOffence(
                id = "1",
                offenceTitle = "Offence 2",
                offenceCode = "ZZ00120",
                wording = "Offence 2",
                offenceLegislation = "Offence 2",
                listingNumber = 0
            )
        ))

        assertThat(result!!.offenceCode, equalTo("ZZ00120"))
    }

    @Test
    fun `Offences with home office code 22222 are ignored`() {
        whenever(detailedOffenceRepository.findByCode("AA00000")).thenReturn(
            DetailedOffence(
                code = "AA00000",
                description = "Test Code",
                startDate = LocalDate.now(),
                endDate = null,
                homeOfficeCode = "222/22",
                homeOfficeDescription = "UNKNOWN",
                legislation = null,
                category = null
            )
        )

        offenceService.findMainOffence(
            listOf(
                HearingOffence(
                    id = "0",
                    offenceTitle = "Unknown",
                    offenceCode = "AA00000",
                    wording = "Unknown",
                    offenceLegislation = "Unknown",
                    listingNumber = 0
                )
            )
        )

        verify(telemetryService).trackEvent(
            "OffenceCodeIgnored",
            mapOf(
                "offenceCode" to "AA00000",
                "homeOfficeCode" to "22222"
            ),
            mapOf()
        )
    }

    @Test
    fun `Offences with CJA code suffix greater 500 are ignored`() {
        whenever(detailedOffenceRepository.findByCode("AA99999")).thenReturn(
            DetailedOffence(
                code = "AA99999",
                description = "Test Code",
                startDate = LocalDate.now(),
                endDate = null,
                homeOfficeCode = "001/00",
                homeOfficeDescription = "UNKNOWN",
                legislation = null,
                category = null
            )
        )

        offenceService.findMainOffence(
            listOf(
                HearingOffence(
                    id = "0",
                    offenceTitle = "Unknown",
                    offenceCode = "AA99999",
                    wording = "Unknown",
                    offenceLegislation = "Unknown",
                    listingNumber = 0
                )
            )
        )

        verify(telemetryService).trackEvent(
            "OffenceCodeIgnored",
            mapOf(
                "offenceCode" to "AA99999",
                "homeOfficeCode" to "00100"
            ),
            mapOf()
        )
    }

    private fun mockS3Client() {
        val mockCsv = """
            ho_offence_code,offence_desc,priority,offence_type,max_custodial_sentence
            00100,Test offence 1,60,Type,30
            00101,Test offence 2,20,Type,240
            00102,Test offence 3,30,Type,100
        """.trimIndent()
        val inputStream = ByteArrayInputStream(mockCsv.toByteArray())
        val responseStream = ResponseInputStream(GetObjectResponse.builder().build(), inputStream)
        val request = GetObjectRequest.builder()
            .bucket(bucketName)
            .key("offence_priority.csv")
            .build()

        whenever(s3Client.getObject(request)).thenReturn(responseStream)
    }
}
