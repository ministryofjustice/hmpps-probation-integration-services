package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import uk.gov.justice.digital.hmpps.integrations.client.ManageOffencesClient
import uk.gov.justice.digital.hmpps.integrations.client.OffencePriority
import uk.gov.justice.digital.hmpps.messaging.HearingOffence
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
class OffenceService(
    private val manageOffencesClient: ManageOffencesClient,
    private val telemetryService: TelemetryService,
    private val s3Client: S3Client,
    @Value("\${s3.bucket.name}") private val bucketName: String
) {
    val priorityMap: Map<String, Int> by lazy { readOffencePrioritiesFromS3() }

    fun getOffenceHomeOfficeCodeByCJACode(cjaCode: String): String =
        manageOffencesClient.getOffenceByCode(cjaCode).homeOfficeStatsCode?.replace("/", "")
            ?: throw IllegalArgumentException("No Home Office code found for CJA code $cjaCode")

    fun findMainOffence(remandedOffences: List<HearingOffence>): HearingOffence? {
        return remandedOffences.mapNotNull {
            val homeOfficeCode = it.offenceCode?.let { code -> getOffenceHomeOfficeCodeByCJACode(code) }

            // If home office offence code is 222/22 ('Not Known') or CJA offence code suffix is 500 or above
            if (homeOfficeCode == "22222" || (it.offenceCode?.takeLast(3)?.toIntOrNull()
                    ?.let { suffix -> suffix >= 500 } == true)
            ) {
                telemetryService.trackEvent(
                    "OffenceCodeIgnored", mapOf(
                        "offenceCode" to it.offenceCode,
                        "homeOfficeCode" to homeOfficeCode
                    )
                )

                null
            } else {
                it to (priorityMap[homeOfficeCode] ?: Int.MAX_VALUE)
            }
        }
            .minByOrNull { (_, priority) -> priority }?.first
    }

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