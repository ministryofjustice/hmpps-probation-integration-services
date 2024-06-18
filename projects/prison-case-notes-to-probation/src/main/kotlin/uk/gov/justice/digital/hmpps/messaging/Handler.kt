package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.exceptions.OffenderNotFoundException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.service.DeliusService
import uk.gov.justice.digital.hmpps.integrations.prison.*
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
import java.net.URI

const val ProcessResettlementPassport = "case-note-resettlement-passport"

@Component
@Channel("prison-case-notes-to-probation-queue")
class Handler(
    val prisonCaseNotesClient: PrisonCaseNotesClient,
    val deliusService: DeliusService,
    val telemetryService: TelemetryService,
    override val converter: NotificationConverter<HmppsDomainEvent>,
    val featureFlags: FeatureFlags
) : NotificationHandler<HmppsDomainEvent> {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @Publish(messages = [Message(name = "prison/case-note-published")])
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        telemetryService.notificationReceived(notification)
        val event = notification.message
        val caseNoteId = event.additionalInformation["caseNoteId"]
        if (caseNoteId == null) {
            log.info("Received ${notification.eventType} for ${event.personReference.findNomsNumber()} without a case note id")
            telemetryService.trackEvent(
                "MissingCaseNoteId",
                mapOf(
                    "eventType" to event.eventType,
                    "nomsNumber" to event.personReference.findNomsNumber()!!
                )
            )
            return
        }

        val prisonCaseNote = try {
            prisonCaseNotesClient.getCaseNote(URI.create(event.detailUrl!!))
        } catch (ex: HttpStatusCodeException) {
            when (ex.statusCode) {
                HttpStatus.NOT_FOUND -> {
                    telemetryService.trackEvent("CaseNoteNotFound", mapOf("detailUrl" to event.detailUrl!!))
                    return
                }

                else -> throw ex
            }
        }

        val reasonToIgnore: Lazy<String?> = lazy {
            (PrisonCaseNoteFilters.filters + PrisonCaseNoteFilter(
                "Processing of Resettlement Passport case notes is disabled"
            ) { it.isResettlementPassport() && !featureFlags.enabled(ProcessResettlementPassport) }).firstOrNull {
                it.predicate.invoke(prisonCaseNote!!)
            }?.reason
        }

        if (prisonCaseNote == null || reasonToIgnore.value != null) {
            val reason = if (prisonCaseNote == null) {
                "case note was not found"
            } else {
                telemetryService.trackEvent(
                    "CaseNoteIgnored",
                    prisonCaseNote.properties() + ("reason" to (reasonToIgnore.value!!))
                )
                reasonToIgnore.value
            }
            log.warn("Ignoring case note id {} and type {} because $reason", caseNoteId, notification.eventType)
            return
        }

        log.debug(
            "Found case note {} of type {} {} in case notes service, now pushing to delius with event id {}",
            caseNoteId,
            prisonCaseNote.type,
            prisonCaseNote.subType,
            prisonCaseNote.eventId
        )

        try {
            deliusService.mergeCaseNote(prisonCaseNote.toDeliusCaseNote(event.occurredAt))
            telemetryService.trackEvent("CaseNoteMerged", prisonCaseNote.properties())
        } catch (e: Exception) {
            telemetryService.trackEvent(
                "CaseNoteMergeFailed",
                prisonCaseNote.properties() + ("exception" to (e.message ?: ""))
            )
            if (e !is OffenderNotFoundException) throw e
        }
    }

    private fun PrisonCaseNote.properties() = mapOf(
        "caseNoteId" to id,
        "type" to type,
        "subType" to subType,
        "eventId" to eventId.toString(),
        "created" to DeliusDateTimeFormatter.format(creationDateTime),
        "occurrence" to DeliusDateTimeFormatter.format(occurrenceDateTime),
        "location" to locationId
    )
}
