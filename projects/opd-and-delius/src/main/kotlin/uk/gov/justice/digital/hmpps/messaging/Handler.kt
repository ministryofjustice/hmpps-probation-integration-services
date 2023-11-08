package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.NsiSubType
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.OpdService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

const val FeatureFlag = "opd-assessment-processing"
const val OpdProduced = "opd.produced"

@Component
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val telemetryService: TelemetryService,
    private val opdService: OpdService,
    private val featureFlags: FeatureFlags
) : NotificationHandler<HmppsDomainEvent> {
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        if (notification.message.eventType != OpdProduced) return
        val opdAssessment = notification.message.opdAssessment()
        if (!featureFlags.enabled(FeatureFlag)) {
            telemetryService.trackEvent("OpdAssessmentIgnored", opdAssessment.telemetryProperties())
            return
        }
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

fun HmppsDomainEvent.opdAssessment() = OpdAssessment(personReference.findCrn()!!, assessmentDate(), assessmentResult())

data class OpdAssessment(
    val crn: String,
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

fun OpdAssessment.telemetryProperties() = mapOf(
    "crn" to crn,
    "date" to DeliusDateTimeFormatter.format(date),
    "result" to result.description
)
