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
    private val personCaseNote: PersonCaseNote,
    override val converter: NotificationConverter<HmppsDomainEvent>,
) : NotificationHandler<HmppsDomainEvent> {

    companion object {
        const val CASE_NOTE_PUBLISHED = "prison.case-note.published"
        const val PRISON_IDENTIFIER_ADDED = "probation-case.prison-identifier.added"
        const val PERSON_CASE_NOTE_CREATED = "person.case-note.created"
        const val PERSON_CASE_NOTE_UPDATED = "person.case-note.updated"
    }

    @Publish(
        messages = [
            Message(name = "prison/case-note-published"),
            Message(title = PRISON_IDENTIFIER_ADDED, payload = Schema(HmppsDomainEvent::class)),
            Message(title = PERSON_CASE_NOTE_CREATED, payload = Schema(HmppsDomainEvent::class)),
            Message(title = PERSON_CASE_NOTE_UPDATED, payload = Schema(HmppsDomainEvent::class)),
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        when (notification.eventType) {
            CASE_NOTE_PUBLISHED -> caseNotePublished.handle(notification.message)
            PRISON_IDENTIFIER_ADDED -> prisonIdentifierAdded.handle(notification.message)
            PERSON_CASE_NOTE_CREATED, PERSON_CASE_NOTE_UPDATED -> personCaseNote.handle(notification.message)
        }
    }
}
