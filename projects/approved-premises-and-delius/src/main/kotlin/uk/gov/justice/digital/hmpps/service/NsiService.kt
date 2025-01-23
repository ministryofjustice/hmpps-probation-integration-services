package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonArrived
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonDeparted
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.ApprovedPremises
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
    fun personArrived(
        person: Person,
        details: PersonArrived,
        ap: ApprovedPremises
    ): Pair<PersonAddress?, PersonAddress>? {
        nsiRepository.findByPersonIdAndExternalReference(person.id, details.residencyRef()) ?: run {
            val team = teamRepository.getApprovedPremisesTeam(details.premises.legacyApCode)
            val staff = staffRepository.getByCode(details.recordedBy.staffCode)
            person.createNsi(APPROVED_PREMISES_RESIDENCE, IN_RESIDENCE, details, details.residencyRef(), staff, team)
            person.createNsi(REHABILITATIVE_ACTIVITY, ACTIVE, details, details.rehabilitativeActivityRef(), staff, team)
            contactService.createContact(
                ContactDetails(
                    date = details.arrivedAt,
                    type = ContactTypeCode.ARRIVED,
                    locationCode = ap.locationCode(),
                    description = "Arrived at ${details.premises.name}",
                    notes = listOfNotNull(
                        details.notes,
                        "For more details, click here: ${details.applicationUrl}"
                    ).joinToString(System.lineSeparator() + System.lineSeparator())
                ),
                person = person,
                eventId = eventRepository.getEvent(person.id, details.eventNumber).id,
                staff = staff,
                team = team,
                probationAreaCode = ap.probationArea.code
            )
            referralService.personArrived(person, ap, details)
            return addressService.updateMainAddress(person, details, ap)
        }
        return null
    }

    fun personDeparted(person: Person, details: PersonDeparted, ap: ApprovedPremises): PersonAddress? {
        nsiRepository.findByPersonIdAndExternalReference(person.id, details.residencyRef())?.let { nsi ->
            nsi.actualEndDate = details.departedAt
            nsi.outcome = referenceDataRepository.referralCompleted()
        }
        nsiRepository.findByPersonIdAndExternalReference(person.id, details.rehabilitativeActivityRef())?.let { nsi ->
            nsi.actualEndDate = details.departedAt
            nsi.status = nsiStatusRepository.getByCode(NsiStatusCode.COMPLETED.code)
            nsi.outcome = referenceDataRepository.endOfEngagementOutcome()
        }
        contactService.createContact(
            ContactDetails(
                date = details.departedAt,
                type = ContactTypeCode.DEPARTED,
                description = "Departed from ${details.premises.name}",
                outcomeCode = ContactOutcome.AP_DEPARTED_PREFIX + details.legacyReasonCode,
                locationCode = ap.locationCode(),
                notes = "For details, see the referral on the AP Service: ${details.applicationUrl}",
                createAlert = false
            ),
            person = person,
            eventId = eventRepository.getEvent(person.id, details.eventNumber).id,
            team = teamRepository.getApprovedPremisesTeam(details.premises.legacyApCode),
            staff = staffRepository.getByCode(details.recordedBy.staffCode),
            probationAreaCode = ap.probationArea.code
        )
        referralService.personDeparted(person, details)
        return addressService.endMainAddress(person, details.departedAt.toLocalDate())
    }

    private fun Person.createNsi(
        nsiType: NsiTypeCode,
        nsiStatus: NsiStatusCode,
        details: PersonArrived,
        externalReference: String,
        staff: Staff,
        team: Team,
    ) {
        val nsi = nsiRepository.save(
            Nsi(
                person = this,
                type = nsiTypeRepository.getByCode(nsiType.code),
                status = nsiStatusRepository.getByCode(nsiStatus.code),
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
    }

    private fun PersonArrived.residencyRef() = EXT_REF_BOOKING_PREFIX + bookingId
    private fun PersonArrived.rehabilitativeActivityRef() = EXT_REF_REHABILITATIVE_ACTIVITY_PREFIX + bookingId
    private fun PersonDeparted.residencyRef() = EXT_REF_BOOKING_PREFIX + bookingId
    private fun PersonDeparted.rehabilitativeActivityRef() = EXT_REF_REHABILITATIVE_ACTIVITY_PREFIX + bookingId
}
