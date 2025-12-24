package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import uk.gov.justice.digital.hmpps.dto.OffenceAndPlea
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

    fun getOffenceHomeOfficeCodeByCJACode(cjaCode: String) =
        manageOffencesClient.getOffenceByCode(cjaCode).homeOfficeStatsCode?.replace("/", "")

    fun findMainOffence(remandedOffences: List<OffenceAndPlea>) =
        remandedOffences.minByOrNull { priorityMap[it.homeOfficeOffenceCode] ?: Int.MAX_VALUE }

    fun getRemandOffences(
        hearingOffences: List<HearingOffence>,
        telemetryProperties: Map<String, String>
    ) = hearingOffences
        .filter { it.judicialResults?.any { r -> r.label == "Remanded in custody" } == true }
        .mapNotNull { offence ->
            offence.offenceCode
                ?.let { offenceCode -> getOffenceHomeOfficeCodeByCJACode(offenceCode) }
                ?.let { homeOfficeCode -> offence.withHomeOfficeCode(homeOfficeCode) }
                ?: run {
                    telemetryService.trackEvent(
                        "MissingHomeOfficeCode", mapOf("offenceCode" to offence.offenceCode) + telemetryProperties
                    )
                    null
                }
        }
        .filter { offence ->
            // If the home office code is 222/22 ('Not Known'), or CJA offence code suffix is 500 or above
            val ignored = offence.homeOfficeOffenceCode == "22222" ||
                (offence.offenceCode.takeLast(3).toIntOrNull()?.let { it >= 500 } == true)
            if (ignored) telemetryService.trackEvent(
                "OffenceCodeIgnored",
                mapOf(
                    "offenceCode" to offence.offenceCode,
                    "homeOfficeCode" to offence.homeOfficeOffenceCode
                ) + telemetryProperties
            )
            !ignored
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