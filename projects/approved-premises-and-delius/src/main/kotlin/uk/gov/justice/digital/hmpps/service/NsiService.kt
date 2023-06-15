package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonArrived
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.PersonDeparted
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.contact.outcome.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiManager
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiStatusRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.TransferReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.getNsiTransferReason
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.getApprovedPremisesTeam

@Service
class NsiService(
    private val nsiRepository: NsiRepository,
    private val nsiTypeRepository: NsiTypeRepository,
    private val nsiStatusRepository: NsiStatusRepository,
    private val nsiManagerRepository: NsiManagerRepository,
    private val personRepository: PersonRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val transferReasonRepository: TransferReasonRepository,
    private val addressService: AddressService,
    private val contactService: ContactService
) {
    @Transactional
    fun personArrived(
        person: Person,
        details: PersonArrived,
        ap: ApprovedPremises
    ) {
        val externalReference = Nsi.EXT_REF_BOOKING_PREFIX + details.bookingId
        nsiRepository.findByExternalReference(externalReference) ?: run {
            personRepository.findForUpdate(person.id)
            nsiRepository.findByExternalReference(externalReference) ?: run {
                val nsi = nsiRepository.save(
                    Nsi(
                        person = person,
                        type = nsiTypeRepository.getByCode(NsiTypeCode.APPROVED_PREMISES_RESIDENCE.code),
                        status = nsiStatusRepository.getByCode(NsiStatusCode.IN_RESIDENCE.code),
                        referralDate = details.applicationSubmittedOn,
                        expectedStartDate = details.arrivedAt,
                        actualStartDate = details.arrivedAt,
                        expectedEndDate = details.expectedDepartureOn,
                        notes = listOfNotNull(
                            details.notes,
                            "For more details, click here: ${details.applicationUrl}"
                        ).joinToString("\n\n"),
                        externalReference = externalReference
                    )
                )
                val team = teamRepository.getApprovedPremisesTeam(details.premises.legacyApCode)
                nsiManagerRepository.save(
                    NsiManager(
                        nsi = nsi,
                        staff = staffRepository.getByCode(details.keyWorker.staffCode),
                        team = team,
                        probationArea = team.probationArea,
                        startDate = details.arrivedAt,
                        transferReason = transferReasonRepository.getNsiTransferReason()
                    )
                )
                addressService.updateMainAddress(person, details, ap)
                contactService.createContact(
                    ContactDetails(
                        date = details.arrivedAt,
                        type = ContactTypeCode.ARRIVED,
                        locationCode = ap.locationCode,
                        notes = listOfNotNull(
                            details.notes,
                            "For more details, click here: ${details.applicationUrl}"
                        ).joinToString("\n\n")
                    ),
                    person = person,
                    staffCode = details.keyWorker.staffCode,
                    probationAreaCode = ap.probationArea.code
                )
            }
        }
    }

    @Transactional
    fun personDeparted(person: Person, details: PersonDeparted, ap: ApprovedPremises) {
        val nsi = nsiRepository.findByExternalReference(Nsi.EXT_REF_BOOKING_PREFIX + details.bookingId)
        nsi?.actualEndDate = details.departedAt
        addressService.endMainAddress(person, details.departedAt.toLocalDate())
        contactService.createContact(
            ContactDetails(
                date = details.departedAt,
                type = ContactTypeCode.DEPARTED,
                outcomeCode = ContactOutcome.AP_DEPARTED_PREFIX + details.legacyReasonCode,
                locationCode = ap.locationCode,
                notes = "For details, see the referral on the AP Service: ${details.applicationUrl}",
                createAlert = false
            ),
            person = person,
            staffCode = details.keyWorker.staffCode,
            probationAreaCode = ap.probationArea.code
        )
    }
}
