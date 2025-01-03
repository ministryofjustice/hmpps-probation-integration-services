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
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.address.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.referralCompleted
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.getByCode
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
        val externalReference = EXT_REF_BOOKING_PREFIX + details.bookingId
        nsiRepository.findByPersonIdAndExternalReference(person.id, externalReference) ?: run {
            val staff = staffRepository.getByCode(details.recordedBy.staffCode)
            val nsi = nsiRepository.save(
                Nsi(
                    person = person,
                    type = nsiTypeRepository.getByCode(NsiTypeCode.APPROVED_PREMISES_RESIDENCE.code),
                    status = nsiStatusRepository.getByCode(NsiStatusCode.IN_RESIDENCE.code),
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
            val team = teamRepository.getApprovedPremisesTeam(details.premises.legacyApCode)
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
        val nsi =
            nsiRepository.findByPersonIdAndExternalReference(person.id, EXT_REF_BOOKING_PREFIX + details.bookingId)
        nsi?.actualEndDate = details.departedAt
        nsi?.outcome = referenceDataRepository.referralCompleted()
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
}
