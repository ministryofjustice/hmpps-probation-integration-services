package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.Cas3ApiClient
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactType.Companion.BOOKING_CANCELLED
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactType.Companion.BOOKING_CONFIRMED
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactType.Companion.BOOKING_PROVISIONAL
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactType.Companion.REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.getByCrn
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.crn
import uk.gov.justice.digital.hmpps.messaging.url
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@Service
class ContactService(
    auditedInteractionService: AuditedInteractionService,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val telemetryService: TelemetryService,
    private val cas3ApiClient: Cas3ApiClient
) : AuditableService(auditedInteractionService) {

    fun createReferralSubmitted(event: HmppsDomainEvent) {
        val details = cas3ApiClient.getApplicationSubmittedDetails(event.url()).eventDetails
        val crn = event.crn()
        val externalReference = details.urn

        if (contactRepository.getByExternalReference(externalReference) != null) {
            telemetryService.trackEvent("Duplicate ApplicationSubmitted event received for crn $crn")
        } else {
            createContact(
                event.occurredAt,
                crn,
                "",
                REFERRAL_SUBMITTED,
                externalReference
            )
        }
    }

    fun createBookingCancelled(event: HmppsDomainEvent) {
        val details = cas3ApiClient.getBookingCancelledDetails(event.url()).eventDetails
        val crn = event.crn()
        createContact(
            event.occurredAt,
            crn,
            "${details.cancellationReason} ${details.cancellationContext} ${details.bookingUrl}",
            BOOKING_CANCELLED,
            details.urn
        )
    }

    fun createBookingConfirmed(event: HmppsDomainEvent) {
        val details = cas3ApiClient.getBookingConfirmedDetails(event.url()).eventDetails
        val crn = event.crn()
        createContact(
            event.occurredAt,
            crn,
            "${details.expectedArrivedAt} ${details.notes} ${details.bookingUrl}",
            BOOKING_CONFIRMED,
            details.urn
        )
    }

    fun createBookingProvisionallyMade(event: HmppsDomainEvent) {
        val crn = event.crn()
        val details = cas3ApiClient.getBookingProvisionallyMade(event.url()).eventDetails
        createContact(
            event.occurredAt,
            crn,
            "${details.expectedArrivedAt} ${details.notes} ${details.bookingUrl}",
            BOOKING_PROVISIONAL,
            details.urn
        )
    }

    fun createContact(
        contactDate: ZonedDateTime,
        crn: String,
        notes: String,
        contactTypeCode: String,
        externalReference: String
    ) =
        audit(BusinessInteractionCode.UPDATE_CONTACT) {
            val person = personRepository.getByCrn(crn)
            contactRepository.save(
                newContact(
                    contactDate,
                    person.id,
                    contactTypeCode,
                    externalReference,
                    notes
                )
            )
        }

    fun newContact(
        occurredAt: ZonedDateTime,
        personId: Long,
        typeCode: String,
        reference: String,
        notes: String
    ): Contact {
        val contactType = contactTypeRepository.findByCode(typeCode) ?: throw NotFoundException(
            "ContactType",
            "code",
            typeCode
        )
        val comDetails = personManagerRepository.findActiveManager(personId) ?: throw NotFoundException(
            "PersonManager",
            "personId",
            personId
        )

        return Contact(
            offenderId = personId,
            type = contactType,
            notes = notes,
            date = occurredAt.toLocalDate(),
            startTime = occurredAt,
            isSensitive = contactType.isSensitive,
            probationAreaId = comDetails.probationAreaId,
            teamId = comDetails.teamId,
            staffId = comDetails.staffId,
            externalReference = reference
        )
    }
}
