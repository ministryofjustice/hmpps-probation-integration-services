package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.channel.Message
import java.time.ZonedDateTime

@Message
data class EmailMessage(
    val id: String,
    val subject: String,
    val bodyContent: String,
    val fromEmailAddress: String,
    val receivedDateTime: ZonedDateTime,
)

private const val EMAIL_FOOTER =
    "This e-mail and any attachments is intended only for the attention of the addressee(s). Its unauthorised use, disclosure, storage or copying is not permitted. If you are not the intended recipient, please destroy all copies and inform the sender by return e-mail. Internet e-mail is not a secure medium. Any reply to this message could be intercepted and read by someone else. Please bear that in mind when deciding whether to send material in response to this message by e-mail. This e-mail (whether you are the sender or the recipient) may be monitored, recorded and retained by the Ministry of Justice. Monitoring / blocking software may be used, and e-mail content may be read at any time. You have a responsibility to ensure laws are not broken when composing or forwarding e-mails and their contents."

internal fun String.withoutFooter() = this.replace(EMAIL_FOOTER, "")
