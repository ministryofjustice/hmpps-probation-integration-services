package uk.gov.justice.digital.hmpps.service

import com.microsoft.graph.models.Message
import com.microsoft.graph.serviceclient.GraphServiceClient
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.EmailMessage
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher

@Service
class MailboxService(
    @Value("\${microsoft-graph.email-address}")
    private val emailAddress: String,
    private val graphServiceClient: GraphServiceClient,
    private val notificationPublisher: NotificationPublisher
) {
    @WithSpan("POLL mailbox", kind = SpanKind.SERVER)
    fun publishUnreadMessagesToQueue() {
        getUnreadMessages().forEach { message ->
            notificationPublisher.publish(message.asNotification())
            message.markAsRead()
        }
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
        }.value

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
