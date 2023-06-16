package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.BookingMade
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.Referral
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.ReferralRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.ReferralSourceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.getByEventNumber
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrn
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.DatasetCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.acceptedDeferredAdmission
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.apReferralSource
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.otherReferralCategory
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.unknownRisk
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ynUnknown
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.staff.getUnallocated
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.getApprovedPremisesTeam
import uk.gov.justice.digital.hmpps.integrations.delius.team.getUnallocatedTeam

@Service
class ReferralService(
    private val referenceDataRepository: ReferenceDataRepository,
    private val referralSourceRepository: ReferralSourceRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val referralRepository: ReferralRepository,
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val contactService: ContactService
) {
    @Transactional
    fun bookingMade(crn: String, details: BookingMade, ap: ApprovedPremises) {
        val person = personRepository.getByCrn(crn)
        val event = eventRepository.getByEventNumber(person.id, details.eventNumber)
        val apTeam = teamRepository.getApprovedPremisesTeam(ap.code.code)
        val apStaff = staffRepository.getUnallocated(apTeam.code)
        val rTeam = teamRepository.getUnallocatedTeam(ap.probationArea.code)
        val rStaff = staffRepository.getByCode(details.bookedBy.staffMember.staffCode)
        referralRepository.save(details.referral(person, event, ap, apTeam, apStaff, rTeam, rStaff))
        contactService.createContact(
            ContactDetails(
                date = details.createdAt,
                type = ContactTypeCode.BOOKING_MADE,
                notes = "To view details of the Approved Premises booking, click here: ${details.applicationUrl}",
                description = "Approved Premises Booking for ${details.premises.name}",
                locationCode = ap.locationCode()
            ),
            person = person,
            staff = rStaff,
            probationAreaCode = ap.probationArea.code
        )
    }

    fun BookingMade.referral(
        person: Person,
        event: Event,
        ap: ApprovedPremises,
        apTeam: Team,
        apStaff: Staff,
        referringTeam: Team,
        referringStaff: Staff
    ): Referral {
        val notes = """
            |This referral was made in the new AP Referral System. 
            |Please follow this link to see the original referral: $applicationUrl
            |**Disclaimer** information about sex offences and gang affiliation unknown
        """.trimIndent()
        val ynUnknown = referenceDataRepository.ynUnknown()
        val riskUnknown = referenceDataRepository.unknownRisk()
        return Referral(
            person = person,
            event = event,
            approvedPremises = ap,
            referralDate = createdAt.toLocalDate(),
            referralDateType = referenceDataRepository.findByCodeAndDatasetCode(
                "CRC",
                DatasetCode.AP_REFERRAL_DATE_TYPE
            ),
            expectedArrivalDate = arrivalOn,
            expectedDepartureDate = departureOn,
            decisionDate = createdAt,
            category = referenceDataRepository.otherReferralCategory(),
            decision = referenceDataRepository.acceptedDeferredAdmission(),
            referralNotes = notes,
            decisionNotes = notes,
            referralSource = referralSourceRepository.getByCode("OTH"),
            sourceType = referenceDataRepository.apReferralSource(),
            reasonForReferral = notes,
            activeArsonRisk = ynUnknown,
            arsonRiskDetails = notes,
            disabilityIssues = ynUnknown,
            disabilityDetails = notes,
            singleRoom = ynUnknown,
            singleRoomDetails = notes,
            sexOffender = true, // This should be updated when info available from AP Service
            gangAffiliated = true, // This should be updated when info available from AP Service
            rohChildren = riskUnknown,
            rohKnownPerson = riskUnknown,
            rohOthers = riskUnknown,
            rohPublic = riskUnknown,
            rohResidents = riskUnknown,
            rohSelf = riskUnknown,
            rohStaff = riskUnknown,
            riskInformation = notes,
            decisionTeamId = apTeam.id,
            decisionStaffId = apStaff.id,
            referringTeamId = referringTeam.id,
            referringStaffId = referringStaff.id
        )
    }
}
