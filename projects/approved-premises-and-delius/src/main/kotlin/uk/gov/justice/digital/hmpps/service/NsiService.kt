package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.BookingMade
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonArrived
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonDeparted
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.getEvent
import uk.gov.justice.digital.hmpps.integrations.delius.contact.outcome.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiStatusCode.ACTIVE
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiStatusCode.IN_RESIDENCE
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiTypeCode.APPROVED_PREMISES_RESIDENCE
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiTypeCode.REHABILITATIVE_ACTIVITY
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.address.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.endOfEngagementOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.referralCompleted
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.getApprovedPremisesTeam
import java.time.ZonedDateTime

@Transactional
@Service
class NsiService(
    private val nsiRepository: NsiRepository,
    private val nsiTypeRepository: NsiTypeRepository,
    private val nsiStatusRepository: NsiStatusRepository,
    private val nsiManagerRepository: NsiManagerRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val transferReasonRepository: TransferReasonRepository,
    private val addressService: AddressService,
    private val contactService: ContactService,
    private val referralService: ReferralService,
    private val referenceDataRepository: ReferenceDataRepository,
    private val eventRepository: EventRepository
) {
    fun preArrival(person: Person, details: BookingMade) {
        val existing = nsiRepository.findByPersonIdAndTypeCodeAndActualEndDateIsNull(
            person.id,
            NsiTypeCode.PRE_RELEASE_ARRIVAL.code
        )
        if (existing.isNotEmpty()) return

        val team = teamRepository.getApprovedPremisesTeam(details.premises.legacyApCode)
        val staff = staffRepository.getByCode(details.bookedBy.staffMember.staffCode)
        createPreArrivalNsi(person, details, staff, team)
    }

    fun personArrived(
        person: Person,
        details: PersonArrived,
        ap: ApprovedPremises
    ): Pair<PersonAddress?, PersonAddress>? {
        nsiRepository.findByPersonIdAndExternalReference(person.id, details.residencyRef()) ?: run {
            val team = teamRepository.getApprovedPremisesTeam(details.premises.legacyApCode)
            val staff = staffRepository.getByCode(details.recordedBy.staffCode)
            val event = eventRepository.getEvent(person.id, details.eventNumber)
            val residence = referralService.personArrived(person, ap, details)
            event.createNsi(APPROVED_PREMISES_RESIDENCE, IN_RESIDENCE, details, details.residencyRef(), staff, team)
            event.createNsi(REHABILITATIVE_ACTIVITY, ACTIVE, details, details.rehabilitativeActivityRef(), staff, team)
            contactService.createContact(
                ContactDetails(
                    date = details.arrivedAt,
                    typeCode = ContactTypeCode.ARRIVED.code,
                    locationCode = ap.locationCode(),
                    description = "Arrived at ${details.premises.name}",
                    notes = listOfNotNull(
                        details.notes,
                        "For more details, click here: ${details.applicationUrl}"
                    ).joinToString(System.lineSeparator() + System.lineSeparator())
                ),
                person = person,
                eventId = event.id,
                staff = staff,
                team = team,
                probationAreaCode = ap.probationArea.code,
                residenceId = residence.id
            )
            return addressService.updateMainAddressOnArrival(person, details, ap)
        }
        return null
    }

    fun personDeparted(person: Person, details: PersonDeparted, ap: ApprovedPremises): PersonAddress? {
        val team = teamRepository.getApprovedPremisesTeam(details.premises.legacyApCode)
        val staff = staffRepository.getByCode(details.recordedBy.staffCode)
        findNsi(person, APPROVED_PREMISES_RESIDENCE, details.residencyRef()).forEach { nsi ->
            nsi.actualEndDate = details.departedAt
            nsi.outcome = referenceDataRepository.referralCompleted()
            nsi.terminationContact(details.departedAt, staff, team)
        }
        findNsi(person, REHABILITATIVE_ACTIVITY, details.rehabilitativeActivityRef()).forEach { nsi ->
            nsi.actualEndDate = details.departedAt
            nsi.status = nsiStatusRepository.getByCode(NsiStatusCode.COMPLETED.code)
            nsi.outcome = referenceDataRepository.endOfEngagementOutcome()
            nsi.statusChangeContact(details.departedAt, staff, team)
            nsi.terminationContact(details.departedAt, staff, team)
        }
        contactService.createContact(
            ContactDetails(
                date = details.departedAt,
                typeCode = ContactTypeCode.DEPARTED.code,
                description = "Departed from ${details.premises.name}",
                outcomeCode = ContactOutcome.AP_DEPARTED_PREFIX + details.legacyReasonCode,
                locationCode = ap.locationCode(),
                notes = "For details, see the referral on the AP Service: ${details.applicationUrl}",
                createAlert = false
            ),
            person = person,
            eventId = eventRepository.getEvent(person.id, details.eventNumber).id,
            team = team,
            staff = staff,
            probationAreaCode = ap.probationArea.code
        )
        referralService.personDeparted(person, details)
        return addressService.endMainAddressOnDeparture(person, details.departedAt.toLocalDate())
    }

    private fun findNsi(person: Person, type: NsiTypeCode, externalReference: String) =
        nsiRepository.findByPersonIdAndExternalReference(person.id, externalReference)?.let { listOf(it) }
        // To handle existing NSIs created manually in Delius prior to arrival/departure moving to CAS1 service:
            ?: nsiRepository.findByPersonIdAndTypeCodeAndActualEndDateIsNull(person.id, type.code)

    private fun Event.createNsi(
        nsiType: NsiTypeCode,
        nsiStatus: NsiStatusCode,
        details: PersonArrived,
        externalReference: String,
        staff: Staff,
        team: Team,
    ) {
        val type = nsiTypeRepository.getByCode(nsiType.code)
        val status = nsiStatusRepository.getByCode(nsiStatus.code)
        val nsi = nsiRepository.save(
            Nsi(
                event = this,
                person = person,
                type = type,
                status = status,
                referralDate = details.applicationSubmittedOn,
                expectedStartDate = details.arrivedAt.toLocalDate(),
                actualStartDate = details.arrivedAt,
                expectedEndDate = details.expectedDepartureOn,
                notes = listOfNotNull(
                    details.notes,
                    "For more details, click here: ${details.applicationUrl}"
                ).joinToString(System.lineSeparator() + System.lineSeparator()),
                externalReference = externalReference
            )
        )
        nsiManagerRepository.save(
            NsiManager(
                nsi = nsi,
                staff = staff,
                team = team,
                probationArea = team.probationArea,
                startDate = details.arrivedAt,
                transferReason = transferReasonRepository.getNsiTransferReason()
            )
        )
        nsi.referralContact(details.arrivedAt, staff, team)
        nsi.statusChangeContact(details.arrivedAt, staff, team)
    }

    private fun Nsi.referralContact(date: ZonedDateTime, staff: Staff, team: Team) {
        contactService.createContact(
            ContactDetails(
                date = date,
                typeCode = ContactTypeCode.NSI_REFERRAL.code,
                createAlert = false
            ),
            person = person,
            eventId = event?.id,
            nsiId = id,
            staff = staff,
            team = team,
            probationAreaCode = team.probationArea.code,
        )
    }

    private fun Nsi.statusChangeContact(date: ZonedDateTime, staff: Staff, team: Team) = status.contactType?.let {
        contactService.createContact(
            ContactDetails(
                date = date,
                typeCode = it.code,
                createAlert = false,
            ),
            person = person,
            eventId = event?.id,
            nsiId = id,
            staff = staff,
            team = team,
            probationAreaCode = team.probationArea.code
        )
    }

    private fun Nsi.terminationContact(date: ZonedDateTime, staff: Staff, team: Team) = contactService.createContact(
        ContactDetails(
            date = date,
            typeCode = ContactTypeCode.NSI_TERMINATED.code,
            notes = "NSI Terminated with Outcome: ${requireNotNull(outcome?.description) { "Terminating NSI with no outcome" }}",
            createAlert = false
        ),
        person = person,
        eventId = event?.id,
        nsiId = id,
        team = team,
        staff = staff,
        probationAreaCode = team.probationArea.code
    )

    private fun Nsi.caseAllocatedContact(date: ZonedDateTime, staff: Staff, team: Team) {
        contactService.createContact(
            ContactDetails(
                date = date,
                typeCode = ContactTypeCode.CASE_ALLOCATED.code,
                createAlert = false
            ),
            person = person,
            nsiId = id,
            staff = staff,
            team = team,
            probationAreaCode = team.probationArea.code,
        )
    }

    private fun createPreArrivalNsi(
        person: Person,
        details: BookingMade,
        staff: Staff,
        team: Team,
    ) {
        val type = nsiTypeRepository.getByCode(NsiTypeCode.PRE_RELEASE_ARRIVAL.code)
        val status = nsiStatusRepository.getByCode(NsiStatusCode.AP_CASE_ALLOCATED.code)
        val nsi = nsiRepository.save(
            Nsi(
                person = person,
                event = null,
                type = type,
                status = status,
                referralDate = details.bookingMadeAt.toLocalDate(),
                expectedStartDate = details.arrivalOn,
                notes = listOfNotNull(
                    "AP placement allocated to ${details.premises.name}",
                    "For more details, click here: ${details.applicationUrl}"
                ).joinToString(System.lineSeparator() + System.lineSeparator()),
                externalReference = details.preArrivalRef()
            )
        )
        nsiManagerRepository.save(
            NsiManager(
                nsi = nsi,
                staff = staff,
                team = team,
                probationArea = team.probationArea,
                startDate = details.bookingMadeAt,
                transferReason = transferReasonRepository.getNsiTransferReason()
            )
        )
        nsi.caseAllocatedContact(details.bookingMadeAt, staff, team)
        nsi.statusChangeContact(details.bookingMadeAt, staff, team)
    }

    private fun PersonArrived.residencyRef() = EXT_REF_BOOKING_PREFIX + bookingId
    private fun PersonArrived.rehabilitativeActivityRef() = EXT_REF_REHABILITATIVE_ACTIVITY_PREFIX + bookingId
    private fun PersonDeparted.residencyRef() = EXT_REF_BOOKING_PREFIX + bookingId
    private fun PersonDeparted.rehabilitativeActivityRef() = EXT_REF_REHABILITATIVE_ACTIVITY_PREFIX + bookingId
    private fun BookingMade.preArrivalRef() = EXT_REF_PREARRIVAL_PREFIX + bookingId
}
