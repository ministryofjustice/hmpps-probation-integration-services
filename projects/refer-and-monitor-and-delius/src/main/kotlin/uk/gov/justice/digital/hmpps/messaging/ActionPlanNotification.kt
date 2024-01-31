package uk.gov.justice.digital.hmpps.messaging

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.exception.UnprocessableException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Nsi
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.EventProcessingResult.Success
import java.time.ZonedDateTime

@Component
class ActionPlanNotification(
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val nsiRepository: NsiRepository
) : DomainEventHandler {
    override val handledEvents = mapOf(
        DomainEventType.ActionPlanApproved to ::actionPlanApproved,
        DomainEventType.ActionPlanSubmitted to ::actionPlanSubmitted
    )

    fun actionPlanApproved(event: HmppsDomainEvent): EventProcessingResult = handle(event) {
        notify(event.actionPlanNotification("Approved"))
    }

    fun actionPlanSubmitted(event: HmppsDomainEvent): EventProcessingResult = handle(event) {
        notify(event.actionPlanNotification("Submitted"))
    }

    fun notify(notification: ActionPlanNotification): EventProcessingResult {
        val nsi = nsiRepository.findByPersonCrnAndExternalReference(notification.crn, notification.referral.urn)
            ?: throw UnprocessableException(
                "Unable to find referral for action plan notification",
                mapOf("crn" to notification.crn, "referralId" to notification.referral.id)
            )
        contactRepository.findNotificationContact(
            nsi.id,
            ContactType.Code.CRSNOTE.value,
            notification.date.toLocalDate()
        ).firstOrNull { c -> c.notes?.contains("Action Plan ${notification.type}") == true } ?: run {
            contactRepository.save(
                nsi.contact(ContactType.Code.CRSNOTE.value, notification.date).addNotes(notification.notes)
            )
        }
        return Success(
            when (notification.type) {
                "Submitted" -> DomainEventType.ActionPlanSubmitted
                "Approved" -> DomainEventType.ActionPlanApproved
                else -> throw IllegalArgumentException("Unexpected Domain Event Type")
            }
        )
    }

    private fun Nsi.contact(type: String, date: ZonedDateTime) = Contact(
        type = contactTypeRepository.getByCode(type),
        person = person,
        nsiId = id,
        eventId = eventId,
        providerId = manager.providerId,
        teamId = manager.teamId,
        staffId = manager.staffId,
        date = date.toLocalDate(),
        startTime = date
    )

    private fun HmppsDomainEvent.contractType() = additionalInformation["contractTypeName"] as String
    private fun HmppsDomainEvent.referralId() = additionalInformation["referralId"] as String
    private fun HmppsDomainEvent.referralReference() = additionalInformation["referralReference"] as String
    private fun HmppsDomainEvent.providerName() = additionalInformation["primeProviderName"] as String
    private fun HmppsDomainEvent.url() = additionalInformation["actionPlanProbationUserUrl"] as String
    private val HmppsDomainEvent.referral
        get() = Referral(referralId(), Provider(providerName()), contractType())

    data class ActionPlanNotification(
        val type: String,
        val crn: String,
        val referral: Referral,
        val referralReference: String,
        val uiUrl: String,
        val date: ZonedDateTime
    ) {
        val notes =
            "Action Plan $type for ${referral.contractType} Referral $referralReference with Prime Provider ${referral.provider.name}${System.lineSeparator()}$uiUrl"
    }

    private fun HmppsDomainEvent.actionPlanNotification(type: String) =
        ActionPlanNotification(
            type,
            personReference.findCrn()!!,
            referral,
            referralReference(),
            url(),
            occurredAt
        )
}
