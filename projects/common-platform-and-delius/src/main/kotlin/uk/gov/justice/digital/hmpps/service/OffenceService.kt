package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import uk.gov.justice.digital.hmpps.integrations.client.OffencePriority
import uk.gov.justice.digital.hmpps.integrations.delius.entity.DetailedOffenceRepository

@Service
class OffenceService(
    private val detailedOffenceRepository: DetailedOffenceRepository,
    private val s3Client: S3Client,
    @Value("\${s3.bucket.name}") private val bucketName: String
) {
    val priorityMap: Map<String, Int> by lazy { readOffencePrioritiesFromS3() }

    fun getOffenceHomeOfficeCodeByCJACode(cjaCode: String): String =
        detailedOffenceRepository.findByCode(cjaCode)?.homeOfficeCode?.replace("/", "")
            ?: throw IllegalArgumentException("No Home Office code found for CJA code $cjaCode")

    private fun readOffencePrioritiesFromS3(): Map<String, Int> {
        val request = GetObjectRequest.builder()
            .bucket(bucketName)
            .key("offence_priority.csv")
            .build()

        return CsvMapper().readerFor(OffencePriority::class.java)
            .with(CsvSchema.emptySchema().withHeader())
            .readValues<OffencePriority>(s3Client.getObject(request))
            .asSequence()
            .associateBy({ it.hoOffenceCode }, { it.priority })
    }
}