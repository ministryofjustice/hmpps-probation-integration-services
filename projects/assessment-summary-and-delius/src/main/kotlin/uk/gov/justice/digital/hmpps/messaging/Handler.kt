package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.oasys.OrdsClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.service.AssessmentSubmitted
import java.net.URI

const val AssessmentSummaryProduced = "assessment.summary.produced"

@Component
class Handler(
    override val converter: NotificationConverter<HmppsDomainEvent>,
    private val ordsClient: OrdsClient,
    private val assessmentSubmitted: AssessmentSubmitted
) : NotificationHandler<HmppsDomainEvent> {
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        if (notification.message.eventType != AssessmentSummaryProduced) return
        notification.message.detailUrl
            ?.let { ordsClient.getAssessmentSummary(URI.create(it)) }
            ?.let { assessmentSubmitted.assessmentSubmitted(it.crn, it.assessments.first()) }
    }
}
