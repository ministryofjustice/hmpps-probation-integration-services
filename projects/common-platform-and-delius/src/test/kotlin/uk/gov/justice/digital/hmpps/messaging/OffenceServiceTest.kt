package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import uk.gov.justice.digital.hmpps.integrations.delius.entity.DetailedOffenceRepository
import uk.gov.justice.digital.hmpps.service.OffenceService
import java.io.ByteArrayInputStream

@ExtendWith(MockitoExtension::class)
internal class OffenceServiceTest {

    @Mock
    lateinit var detailedOffenceRepository: DetailedOffenceRepository

    @Mock
    lateinit var s3Client: S3Client

    private val bucketName = "bucket-name"

    private lateinit var offenceService: OffenceService

    @BeforeEach
    fun setUp() {
        offenceService = OffenceService(detailedOffenceRepository, s3Client, bucketName)
    }

    @Test
    fun `csv is parsed into priority map`() {
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

        val priorityMap = offenceService.priorityMap

        assertThat(priorityMap, equalTo(mapOf("00100" to 60, "00101" to 20, "00102" to 30)))
    }
}
