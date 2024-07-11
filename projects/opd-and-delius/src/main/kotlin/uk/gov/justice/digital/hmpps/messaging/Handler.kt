package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.Schema
import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.entity.NsiSubType
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.service.OpdService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

const val OpdProduced = "opd.produced"

@Component
@Channel("opd-and-delius-queue")
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    private val opdService: OpdService,
) : NotificationHandler<HmppsDomainEvent> {
    @Publish(messages = [Message(title = OpdProduced, payload = Schema(HmppsDomainEvent::class))])
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        if (notification.message.eventType != OpdProduced) return
        val opdAssessment = notification.message.opdAssessment()
        when (opdAssessment.result) {
            OpdAssessment.Result.SCREENED_IN, OpdAssessment.Result.SCREENED_IN_OVERRIDE -> {
                opdService.processAssessment(notification.message.opdAssessment())
                telemetryService.trackEvent("OpdAssessmentScreenedIn", opdAssessment.telemetryProperties())
            }

            else -> telemetryService.trackEvent("OpdAssessmentScreenedOut", opdAssessment.telemetryProperties())
        }
    }
}

fun HmppsDomainEvent.assessmentDate() = ZonedDateTime.parse(additionalInformation["dateCompleted"] as String)

fun HmppsDomainEvent.override() = OpdAssessment.Override.of(additionalInformation["opdScreenOutOverride"] as String)
fun HmppsDomainEvent.assessmentResult() =
    OpdAssessment.Result.of(additionalInformation["opdResult"] as String, override())

fun PersonReference.nomisId(): String? = findNomsNumber() ?: identifiers.firstOrNull { it.type == "nomisId" }?.value

fun HmppsDomainEvent.opdAssessment() =
    OpdAssessment(
        personReference.findCrn()?.ifEmpty { null },
        personReference.nomisId()?.ifEmpty { null },
        assessmentDate(),
        assessmentResult()
    )

data class OpdAssessment(
    val crn: String?,
    val noms: String?,
    val date: ZonedDateTime,
    val result: Result
) {
    enum class Result(val description: String, val subTypeCode: NsiSubType.Code?) {
        SCREENED_IN("Screened In", NsiSubType.Code.COMMUNITY_PATHWAY),
        SCREENED_IN_OVERRIDE("Screened In - with override", NsiSubType.Code.COMMUNITY_PATHWAY_OVERRIDE),
        SCREENED_OUT("Screened Out", null);

        companion object {
            fun of(value: String, override: Override) = when {
                override == Override.YES -> SCREENED_IN_OVERRIDE
                "SCREEN IN".equals(value, true) -> SCREENED_IN
                else -> SCREENED_OUT
            }
        }
    }

    enum class Override {
        YES, NO;

        companion object {
            fun of(value: String) = entries.firstOrNull { it.name.equals(value, true) } ?: NO
        }
    }

    val notes = """
        |OPD Assessment Date: ${DeliusDateTimeFormatter.format(date)}
        |OPD Result: ${result.description}
        |This notes entry was automatically created by the system - ${DeliusDateTimeFormatter.format(ZonedDateTime.now())}
    """.trimMargin()
}

fun OpdAssessment.telemetryProperties() = listOfNotNull(
    crn?.let { "crn" to it },
    noms?.let { "noms" to it },
    "date" to DeliusDateTimeFormatter.format(date),
    "result" to result.description
).toMap()