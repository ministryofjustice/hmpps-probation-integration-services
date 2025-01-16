package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.Schema
import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification

@Component
@Channel("prison-case-notes-to-probation-queue")
class Handler(
    private val caseNotePublished: CaseNotePublished,
    private val prisonIdentifierAdded: PrisonIdentifierAdded,
    override val converter: NotificationConverter<HmppsDomainEvent>,
) : NotificationHandler<HmppsDomainEvent> {

    companion object {
        const val CASE_NOTE_PUBLISHED = "prison.case-note.published"
        const val PRISON_IDENTIFIER_ADDED = "probation-case.prison-identifier.added"
    }

    @Publish(
        messages = [
            Message(name = "prison/case-note-published"),
            Message(title = "probation-case.prison-identifier.added", payload = Schema(HmppsDomainEvent::class)),
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        when (notification.eventType) {
            CASE_NOTE_PUBLISHED -> caseNotePublished.handle(notification.message)
            PRISON_IDENTIFIER_ADDED -> prisonIdentifierAdded.handle(notification.message)
        }
    }
}
