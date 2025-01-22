package uk.gov.justice.digital.hmpps.messaging

import com.asyncapi.kotlinasyncapi.annotation.Schema
import com.asyncapi.kotlinasyncapi.annotation.channel.Channel
import com.asyncapi.kotlinasyncapi.annotation.channel.Message
import com.asyncapi.kotlinasyncapi.annotation.channel.Publish
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.integrations.prison.CaseNoteTypesOfInterest
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.Handler.Companion.PERSON_CASE_NOTE_CREATED
import uk.gov.justice.digital.hmpps.messaging.Handler.Companion.PERSON_CASE_NOTE_UPDATED

@Component
@Channel("prison-case-notes-to-probation-queue")
class Handler(
    private val prisonIdentifierAdded: PrisonIdentifierAdded,
    private val personCaseNote: PersonCaseNote,
    override val converter: NotificationConverter<HmppsDomainEvent>,
) : NotificationHandler<HmppsDomainEvent> {

    companion object {
        const val PRISON_IDENTIFIER_ADDED = "probation-case.prison-identifier.added"
        const val PERSON_CASE_NOTE_CREATED = "person.case-note.created"
        const val PERSON_CASE_NOTE_UPDATED = "person.case-note.updated"
    }

    @Publish(
        messages = [
            Message(title = PRISON_IDENTIFIER_ADDED, payload = Schema(HmppsDomainEvent::class)),
            Message(title = PERSON_CASE_NOTE_CREATED, payload = Schema(HmppsDomainEvent::class)),
            Message(title = PERSON_CASE_NOTE_UPDATED, payload = Schema(HmppsDomainEvent::class)),
        ]
    )
    override fun handle(notification: Notification<HmppsDomainEvent>) {
        when {
            notification.eventType == PRISON_IDENTIFIER_ADDED -> prisonIdentifierAdded.handle(notification.message)
            notification.isCaseNoteOfInterest() -> personCaseNote.handle(notification.message)
        }
    }
}

private fun Notification<*>.isCaseNoteOfInterest(): Boolean =
    (eventType == PERSON_CASE_NOTE_CREATED || eventType == PERSON_CASE_NOTE_UPDATED) && typeIsOfInterest()

val Notification<*>.type get() = attributes["type"]?.value
val Notification<*>.subType get() = attributes["subType"]?.value

private fun Notification<*>.typeIsOfInterest() = CaseNoteTypesOfInterest.verifyOfInterest(type!!, subType!!)