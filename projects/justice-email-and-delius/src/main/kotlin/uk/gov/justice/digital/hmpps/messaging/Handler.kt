package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.Schema
import org.openfolder.kotlinasyncapi.annotation.channel.Channel
import org.openfolder.kotlinasyncapi.annotation.channel.Message
import org.openfolder.kotlinasyncapi.annotation.channel.Publish
import org.springframework.ldap.NameNotFoundException
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQueryBuilder.query
import org.springframework.ldap.query.SearchScope
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.audit.BusinessInteractionCode.ADD_CONTACT
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.entity.ContactType.Code.EMAIL_TEXT_FROM_OTHER
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Component
@Channel("justice-email-and-delius-queue")
class Handler(
    auditedInteractionService: AuditedInteractionService,
    override val converter: NotificationConverter<EmailMessage>,
    private val telemetryService: TelemetryService,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val staffRepository: StaffRepository,
    private val ldapTemplate: LdapTemplate,
) : NotificationHandler<EmailMessage>, AuditableService(auditedInteractionService) {
    @Publish(messages = [Message(title = "email-message", payload = Schema(EmailMessage::class))])
    override fun handle(notification: Notification<EmailMessage>) = audit(ADD_CONTACT) { audit ->
        telemetryService.notificationReceived(notification)
        val message = notification.message

        val crn = message.extractCrn()
        val emailAddress =
            message.fromEmailAddress.takeIf { it.endsWith("@justice.gov.uk") || it.endsWith("@digital.justice.gov.uk") }
                ?: throw IllegalArgumentException("Email address does not end with @justice.gov.uk or @digital.justice.gov.uk")
        val person = personRepository.getByCrn(crn)
        val manager = personManagerRepository.getManager(person.id)
        val staffId = findStaffIdForEmailAddress(emailAddress) ?: manager.staffId
        val contact = contactRepository.save(
            Contact(
                personId = person.id,
                externalReference = "urn:uk:gov:hmpps:justice-email:${message.id}",
                type = contactTypeRepository.getByCode(EMAIL_TEXT_FROM_OTHER),
                date = message.receivedDateTime,
                startTime = message.receivedDateTime,
                notes = message.bodyContent,
                staffId = staffId,
                teamId = manager.teamId,
                providerId = manager.providerId,
            )
        )
        audit["contactId"] = contact.id

        telemetryService.trackEvent(
            "CreatedContact", mapOf(
                "crn" to crn,
                "staffId" to staffId.toString(),
                "contactId" to contact.id.toString(),
                "messageId" to message.id,
            )
        )
    }

    private fun EmailMessage.extractCrn(): String {
        val crns = Regex("[A-Za-z][0-9]{6}").findAll(subject)
        return when (crns.count()) {
            1 -> crns.single().value.uppercase()
            0 -> throw IllegalArgumentException("No CRN in message subject")
            else -> throw IllegalArgumentException("Multiple CRNs in message subject")
        }
    }

    private fun findStaffIdForEmailAddress(emailAddress: String): Long? {
        val matchingStaffIds = try {
            ldapTemplate
                .search(query()
                    .attributes("cn")
                    .searchScope(SearchScope.ONELEVEL)
                    .where("objectclass").`is`("inetOrgPerson")
                    .and("objectclass").`is`("top")
                    .and("mail").`is`(emailAddress),
                    AttributesMapper { it["cn"]?.get()?.toString() })
                .filterNotNull()
                .mapNotNull { staffRepository.findByUserUsername(it)?.id }
        } catch (_: NameNotFoundException) {
            return null
        }

        return when (matchingStaffIds.size) {
            0 -> null
            1 -> matchingStaffIds.single()
            else -> error("Multiple staff records found for $emailAddress")
        }
    }
}