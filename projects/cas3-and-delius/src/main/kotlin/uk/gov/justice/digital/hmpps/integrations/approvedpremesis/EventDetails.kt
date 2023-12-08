package uk.gov.justice.digital.hmpps.integrations.approvedpremesis

import uk.gov.justice.digital.hmpps.datetime.DeliusDateFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactType
import java.time.ZonedDateTime
import java.util.LinkedList

data class EventDetails<out T : Cas3Event>(
    val id: String,
    val timestamp: ZonedDateTime,
    val eventType: String,
    val eventDetails: T,
)

data class ApplicationSubmitted(
    val applicationId: String,
) : Cas3Event {
    override val urn = "urn:hmpps:cas3:application-submitted:$applicationId"
    override val contactTypeCode = ContactType.REFERRAL_SUBMITTED
    override val noteText = ""
}

interface Cas3Event {
    val urn: String
    val noteText: String
    val contactTypeCode: String
}

data class BookingCancelled(
    val applicationId: String?,
    val applicationUrl: String?,
    val bookingId: String,
    val bookingUrl: String,
    val cancellationReason: String,
    val cancellationContext: String?,
    val notes: String?,
) : Cas3Event {
    override val urn = "urn:hmpps:cas3:booking-cancelled:$bookingId"
    override val noteText =
        listOfNotNull(cancellationReason, cancellationContext, notes).joinToString(System.lineSeparator())
    override val contactTypeCode = ContactType.BOOKING_CANCELLED
}

data class BookingProvisional(
    val applicationId: String?,
    val applicationUrl: String?,
    val bookingId: String,
    val bookingUrl: String,
    val expectedArrivedAt: ZonedDateTime,
    val notes: String,
) : Cas3Event {
    override val urn = "urn:hmpps:cas3:booking-provisionally-made:$bookingId"
    override val noteText =
        listOfNotNull(
            "Expected arrival date: ${DeliusDateFormatter.format(expectedArrivedAt)}",
            notes,
        ).joinToString(System.lineSeparator())
    override val contactTypeCode = ContactType.BOOKING_PROVISIONAL
}

data class BookingConfirmed(
    val applicationId: String?,
    val applicationUrl: String?,
    val bookingId: String,
    val bookingUrl: String,
    val expectedArrivedAt: ZonedDateTime,
    val notes: String,
) : Cas3Event {
    override val urn = "urn:hmpps:cas3:booking-confirmed:$bookingId"
    override val noteText =
        listOfNotNull(
            "Expected arrival date: ${DeliusDateFormatter.format(expectedArrivedAt)}",
            notes,
        ).joinToString(System.lineSeparator())
    override val contactTypeCode = ContactType.BOOKING_CONFIRMED
}

data class PersonArrived(
    val applicationId: String?,
    val applicationUrl: String?,
    val bookingId: String,
    val bookingUrl: String,
    val arrivedAt: ZonedDateTime,
    val notes: String,
    val premises: Address,
) : Cas3Event {
    override val urn = "urn:hmpps:cas3:person-arrived:$bookingId"
    override val noteText =
        listOfNotNull(
            "Arrival date: ${DeliusDateFormatter.format(arrivedAt)}",
            "Arrival address: ${premises.inNotes()}",
            notes,
        ).joinToString(System.lineSeparator())
    override val contactTypeCode = ContactType.PERSON_ARRIVED
}

data class PersonDeparted(
    val applicationId: String?,
    val applicationUrl: String?,
    val bookingId: String,
    val bookingUrl: String,
    val departedAt: ZonedDateTime,
    val notes: String,
    val reason: String,
    val reasonDetail: String?,
    val moveOnCategory: Category,
) : Cas3Event {
    override val urn = "urn:hmpps:cas3:person-departed:$bookingId"
    override val noteText =
        listOfNotNull(
            "Departure date: ${DeliusDateFormatter.format(departedAt)}",
            reason,
            reasonDetail,
            moveOnCategory.description,
            notes,
        ).joinToString(System.lineSeparator())
    override val contactTypeCode = ContactType.PERSON_DEPARTED
}

data class Address(
    val addressLine1: String,
    val addressLine2: String?,
    val postcode: String,
    val town: String?,
    val region: String,
) {
    val addressLines: AddressLines
        get() {
            val lines = LinkedList(addressLine1.chunked(35) + (addressLine2?.chunked(35) ?: listOf()))
            return if (lines.size < 3) {
                AddressLines(null, lines.pop(), lines.firstOrNull())
            } else {
                AddressLines(lines.pop(), lines.pop(), lines.pop())
            }
        }

    fun inNotes(): String {
        return listOfNotNull(addressLine1, addressLine2, postcode, town, region).joinToString(" ")
    }
}

data class AddressLines(
    val buildingName: String?,
    val streetName: String,
    val district: String?,
)

data class Category(
    val description: String,
)
