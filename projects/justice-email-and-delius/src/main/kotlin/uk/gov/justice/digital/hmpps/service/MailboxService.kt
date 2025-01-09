package uk.gov.justice.digital.hmpps.service

import com.microsoft.graph.models.EmailAddress
import com.microsoft.graph.models.ItemBody
import com.microsoft.graph.models.Message
import com.microsoft.graph.models.Recipient
import com.microsoft.graph.serviceclient.GraphServiceClient
import com.microsoft.graph.users.item.sendmail.SendMailPostRequestBody
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.EmailMessage
import uk.gov.justice.digital.hmpps.messaging.UnableToCreateContactFromEmail
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
class MailboxService(
    @Value("\${microsoft-graph.email-address}")
    private val emailAddress: String,
    private val graphServiceClient: GraphServiceClient,
    private val notificationPublisher: NotificationPublisher,
    private val telemetryService: TelemetryService,
) {
    @WithSpan("POLL mailbox", kind = SpanKind.SERVER)
    fun publishUnreadMessagesToQueue() {
        getUnreadMessages()
            .ifEmpty { return }
            .also { telemetryService.trackEvent("ReceivedMessages", mapOf("count" to it.size.toString())) }
            .forEach {
                notificationPublisher.publish(it.asNotification())
                it.markAsRead()
            }
    }

    @EventListener(UnableToCreateContactFromEmail::class)
    fun onUnableToCreateContactFromEmail(event: UnableToCreateContactFromEmail) {
        val toEmailAddress = EmailAddress().apply { address = event.email.fromEmailAddress }
        val message = Message().apply {
            subject = "Unable to create contact from email"
            body = ItemBody().apply { content = "Reason for the contact not being created: ${event.reason}" }
            toRecipients = listOf(Recipient().apply { emailAddress = toEmailAddress })
        }
        graphServiceClient.me().sendMail().post(SendMailPostRequestBody().apply { setMessage(message) })
        telemetryService.trackEvent(
            "UnableToCreateContactFromEmail",
            mapOf("emailId" to event.email.id, "reason" to event.reason)
        )
    }

    private fun getUnreadMessages() = graphServiceClient
        .users()
        .byUserId(emailAddress)
        .mailFolders()
        .byMailFolderId("inbox")
        .messages()
        .get { request ->
            request.queryParameters.top = 10
            request.queryParameters.filter = "isRead ne true"
            request.queryParameters.select = arrayOf("subject", "from", "id", "receivedDateTime", "body", "isRead")
            request.queryParameters.orderby = arrayOf("receivedDateTime DESC")
        }.value ?: emptyList()

    private fun Message.markAsRead() {
        graphServiceClient
            .users()
            .byUserId(emailAddress)
            .messages()
            .byMessageId(id)
            .patch(Message().apply { isRead = true })
    }

    private fun Message.asNotification() = Notification(
        message = EmailMessage(
            id = id,
            subject = subject,
            bodyContent = body.content,
            fromEmailAddress = from.emailAddress.address,
            receivedDateTime = receivedDateTime.toZonedDateTime(),
        ),
        attributes = MessageAttributes("email.message.received")
    )
}
