package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.datetime.DeliusDateFormatter
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.datetime.toDeliusDate
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.*
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.contact.outcome.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrn
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.*
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.staff.getUnallocated
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.getApprovedPremisesTeam
import uk.gov.justice.digital.hmpps.integrations.delius.team.getUnallocatedTeam
import uk.gov.justice.digital.hmpps.model.ApReferral
import uk.gov.justice.digital.hmpps.model.ExistingReferrals
import uk.gov.justice.digital.hmpps.model.ReferralDetail
import java.time.ZonedDateTime

@Transactional
@Service
class ReferralService(
    private val referenceDataRepository: ReferenceDataRepository,
    private val referralSourceRepository: ReferralSourceRepository,
    private val moveOnCategoryRepository: MoveOnCategoryRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val referralRepository: ReferralRepository,
    private val residenceRepository: ResidenceRepository,
    private val preferredResidenceRepository: PreferredResidenceRepository,
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val registrationRepository: RegistrationRepository,
    private val contactService: ContactService
) {
    fun bookingMade(person: Person, details: BookingMade, ap: ApprovedPremises) {
        val event = eventRepository.getEvent(person.id, details.eventNumber)
        val apTeam = teamRepository.getApprovedPremisesTeam(ap.code.code)
        val apStaff = staffRepository.getUnallocated(apTeam.code)
        val rTeam = teamRepository.getUnallocatedTeam(ap.probationArea.code)
        val rStaff = staffRepository.getByCode(details.bookedBy.staffMember.staffCode)
        val findReferral = {
            referralRepository.findByPersonIdAndExternalReference(person.id, EXT_REF_BOOKING_PREFIX + details.bookingId)
        }
        findReferral() ?: run {
            eventRepository.findForUpdate(event.id)
            findReferral() ?: run {
                referralRepository.save(details.referral(person, event, ap, apTeam, apStaff, rTeam, rStaff))
                contactService.createContact(
                    ContactDetails(
                        date = details.bookingMadeAt,
                        typeCode = ContactTypeCode.BOOKING_MADE.code,
                        notes = """
                            Expected arrival: ${DeliusDateFormatter.format(details.arrivalOn)}
                            Expected departure: ${DeliusDateFormatter.format(details.departureOn)}
                            
                            To view details of the Approved Premises booking, click here: ${details.applicationUrl}
                            """.trimIndent(),
                        description = "Approved Premises Booking for ${details.premises.name}",
                        locationCode = ap.locationCode(),
                        externalReference = details.externalReference()
                    ),
                    person = person,
                    eventId = event.id,
                    staff = rStaff,
                    team = rTeam,
                    probationAreaCode = ap.probationArea.code
                )
            }
        }
    }

    fun bookingChanged(crn: String, details: BookingChanged, ap: ApprovedPremises) {
        val person = personRepository.getByCrn(crn)
        val existing = getReferralAndResidence(person, EXT_REF_BOOKING_PREFIX + details.bookingId)
        contactService.createContact(
            ContactDetails(
                date = details.changedAt,
                typeCode = ContactTypeCode.BOOKING_CHANGED.code,
                locationCode = ap.locationCode(),
                description = "Booking changed for ${details.premises.name}",
                notes = listOfNotNull(
                    "The expected arrival and/or departure dates for the booking have changed.",
                    "Previous: ${existing.referral.expectedArrivalDate.toDeliusDate()}${existing.referral.expectedDepartureDate?.let { " to ${it.toDeliusDate()}" }}",
                    "Current: ${details.arrivalOn.toDeliusDate()} to ${details.departureOn.toDeliusDate()}",
                    "For more details, click here: ${details.applicationUrl}"
                ).joinToString(System.lineSeparator() + System.lineSeparator()),
                externalReference = details.externalReference()
            ),
            person = person,
            eventId = existing.referral.eventId,
            staff = staffRepository.getByCode(details.changedBy.staffCode),
            probationAreaCode = ap.probationArea.code,
        )
        existing.referral.apply {
            expectedArrivalDate = details.arrivalOn
            expectedDepartureDate = details.departureOn
            decisionId = referenceDataRepository.acceptedDeferredAdmission().id
        }
        existing.residence?.apply {
            expectedDepartureDate = details.departureOn
        }
    }

    fun bookingCancelled(crn: String, details: BookingCancelled, ap: ApprovedPremises) {
        val person = personRepository.getByCrn(crn)
        val externalReference = EXT_REF_BOOKING_PREFIX + details.bookingId
        val referral = findReferral(person, externalReference)?.also {
            if (preferredResidenceRepository.existsByApprovedPremisesReferralId(it.id)) {
                preferredResidenceRepository.deleteByApprovedPremisesReferralId(it.id)
            }

            val residence = residenceRepository.findByReferralId(it.id)
            if (residence == null) referralRepository.delete(it)
            else throw IgnorableMessageException(
                "Cannot cancel booking as residency recorded",
                listOfNotNull(
                    "externalReference" to externalReference,
                    "arrivedAt" to DeliusDateTimeFormatter.format(residence.arrivalDate),
                    residence.departureDate?.let { date -> "departedAt" to DeliusDateTimeFormatter.format(date) }
                ).toMap()
            )
        }
        contactService.createContact(
            ContactDetails(
                date = details.cancellationRecordedAt,
                cancellationRecordedAt = details.cancelledAtDate,
                typeCode = ContactTypeCode.BOOKING_CANCELLED.code,
                locationCode = ap.locationCode(),
                description = "Booking cancelled for ${details.premises.name}",
                notes = listOfNotNull(
                    details.cancellationReason,
                    "For more details, click here: ${details.applicationUrl}"
                ).joinToString(System.lineSeparator() + System.lineSeparator()),
                externalReference = details.externalReference()
            ),
            person = person,
            eventId = referral?.eventId ?: eventRepository.getEvent(person.id, details.eventNumber).id,
            staff = staffRepository.getByCode(details.cancelledBy.staffCode),
            probationAreaCode = ap.probationArea.code
        )
    }

    fun personNotArrived(person: Person, ap: ApprovedPremises, dateTime: ZonedDateTime, details: PersonNotArrived) {
        val referral = getReferral(person, EXT_REF_BOOKING_PREFIX + details.bookingId)
        referral.nonArrivalDate = dateTime.toLocalDate()
        referral.nonArrivalNotes = details.notes
        referral.nonArrivalReasonId =
            referenceDataRepository.findByCodeAndDatasetCode("D", DatasetCode.AP_NON_ARRIVAL_REASON)?.id
        contactService.createContact(
            ContactDetails(
                date = dateTime,
                typeCode = ContactTypeCode.NOT_ARRIVED.code,
                locationCode = ap.locationCode(),
                description = details.reason,
                outcomeCode = ContactOutcome.AP_NON_ARRIVAL_PREFIX + details.reasonCode,
                notes = listOfNotNull(
                    details.notes,
                    "For more details, click here: ${details.applicationUrl}"
                ).joinToString(System.lineSeparator() + System.lineSeparator()),
                externalReference = details.externalReference()
            ),
            person = person,
            eventId = referral.eventId,
            staff = staffRepository.getByCode(details.recordedBy.staffCode),
            probationAreaCode = ap.probationArea.code
        )
    }

    fun personArrived(person: Person, ap: ApprovedPremises, details: PersonArrived): Residence {
        val existing = getReferralAndResidence(person, EXT_REF_BOOKING_PREFIX + details.bookingId)
        existing.referral.admissionDate = details.arrivedAt.toLocalDate()

        val residence = existing.residence?.apply {
            approvedPremisesId = ap.id
            arrivalDate = details.arrivedAt
            expectedDepartureDate = details.expectedDepartureOn
        } ?: Residence(
            personId = person.id,
            referralId = existing.referral.id,
            approvedPremisesId = ap.id,
            arrivalDate = details.arrivedAt,
            expectedDepartureDate = details.expectedDepartureOn,
            arrivalNotes = "This residence is being managed in the AP Referral Service. Please Do NOT make any updates to the record using Delius. Thank you.",
        )
        return residenceRepository.save(residence)
    }

    fun personDeparted(person: Person, details: PersonDeparted) {
        val externalReference = EXT_REF_BOOKING_PREFIX + details.bookingId
        val referral = getReferral(person, externalReference)
        val residence = residenceRepository.findByReferralId(referral.id) ?: throw IgnorableMessageException(
            "Residence not found",
            mapOf("crn" to person.crn, "externalReference" to externalReference)
        )
        residence.departureDate = details.departedAt
        residence.departureReasonId = referenceDataRepository.findByCodeAndDatasetCode(
            details.legacyReasonCode,
            DatasetCode.AP_DEPARTURE_REASON
        )?.id
        residence.moveOnCategoryId =
            moveOnCategoryRepository.findByCode(details.destination.moveOnCategory.legacyCode)?.id
    }

    fun findExistingReferrals(crn: String): ExistingReferrals {
        val person = personRepository.getByCrn(crn)
        val referrals = referralRepository.findAllByPersonId(person.id).map { it.apReferral() }
        return ExistingReferrals(person.crn, referrals)
    }

    fun getReferral(person: Person, externalReference: String): Referral {
        val referral: Referral = findReferral(person, externalReference) ?: throw IgnorableMessageException(
            "Referral Not Found",
            mapOf("crn" to person.crn, "externalReference" to externalReference)
        )
        if (referral.approvedPremisesId == null) {
            throw IgnorableMessageException(
                "Approved Premises unlinked from Referral",
                mapOf("crn" to person.crn, "externalReference" to externalReference)
            )
        }
        return referral
    }

    private fun getReferralAndResidence(
        person: Person,
        externalReference: String
    ) = referralRepository.findReferralDetail(person.crn, externalReference)
        ?.also {
            if (it.referral.approvedPremisesId == null) {
                throw IgnorableMessageException(
                    "Approved Premises unlinked from Referral",
                    mapOf("crn" to person.crn, "externalReference" to externalReference)
                )
            }
        }
        ?: throw IgnorableMessageException(
            "Referral Not Found",
            mapOf("crn" to person.crn, "externalReference" to externalReference)
        )

    fun findReferral(person: Person, externalReference: String): Referral? =
        referralRepository.findByPersonIdAndExternalReference(person.id, externalReference)

    fun getReferralDetails(crn: String, bookingId: String): ReferralDetail =
        referralRepository.findReferralDetail(crn, EXT_REF_BOOKING_PREFIX + bookingId)?.let {
            ReferralDetail(
                ApReferral(
                    it.referral.referralDate,
                    it.referral.expectedArrivalDate,
                    it.referral.expectedDepartureDate,
                    it.referral.decisionDate,
                    uk.gov.justice.digital.hmpps.model.ApprovedPremises(it.premises.code.description)
                ),
                it.residence?.arrivalDate,
                it.residence?.departureDate
            )
        } ?: throw NotFoundException("Booking $bookingId for crn $crn not found")

    private fun BookingMade.referral(
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
        """.trimMargin()
        val ynUnknown = referenceDataRepository.ynUnknown()
        val riskUnknown = referenceDataRepository.unknownRisk()
        return Referral(
            personId = person.id,
            eventId = event.id,
            approvedPremisesId = ap.id,
            referralDate = applicationSubmittedOn?.toLocalDate() ?: bookingMadeAt.toLocalDate(),
            referralDateTypeId = checkNotNull(
                referenceDataRepository.findByCodeAndDatasetCode(
                    "CRC",
                    DatasetCode.AP_REFERRAL_DATE_TYPE
                )
            ).id,
            expectedArrivalDate = arrivalOn,
            expectedDepartureDate = departureOn,
            decisionDate = bookingMadeAt,
            categoryId = referenceDataRepository.referralCategory(
                ApprovedPremisesCategoryCode.from(
                    sentenceType,
                    releaseType
                ).value
            ).id,
            referralGroupId = referenceDataRepository.findApprovedPremisesGroup(ap.id)?.id,
            decisionId = referenceDataRepository.acceptedDeferredAdmission().id,
            referralNotes = EXT_REF_BOOKING_PREFIX + bookingId + System.lineSeparator() + notes,
            decisionNotes = notes,
            referralSourceId = referralSourceRepository.getByCode("OTH").id,
            sourceTypeId = referenceDataRepository.apReferralSource().id,
            reasonForReferral = notes,
            activeArsonRiskId = ynUnknown.id,
            arsonRiskDetails = notes,
            disabilityIssuesId = ynUnknown.id,
            disabilityDetails = notes,
            singleRoomId = ynUnknown.id,
            singleRoomDetails = notes,
            sexOffender = registrationRepository.existsByPersonIdAndTypeCode(
                person.id,
                RegisterType.Code.SEX_OFFENCE.value
            ),
            gangAffiliated = registrationRepository.existsByPersonIdAndTypeCode(
                person.id,
                RegisterType.Code.GANG_AFFILIATION.value
            ),
            rohChildrenId = riskUnknown.id,
            rohKnownPersonId = riskUnknown.id,
            rohOthersId = riskUnknown.id,
            rohPublicId = riskUnknown.id,
            rohResidentsId = riskUnknown.id,
            rohSelfId = riskUnknown.id,
            rohStaffId = riskUnknown.id,
            riskInformation = notes,
            decisionTeamId = apTeam.id,
            decisionStaffId = apStaff.id,
            referringTeamId = referringTeam.id,
            referringStaffId = referringStaff.id,
            externalReference = EXT_REF_BOOKING_PREFIX + bookingId
        )
    }

    private fun ReferralWithAp.apReferral() = ApReferral(
        referral.referralDate,
        referral.expectedArrivalDate,
        referral.expectedDepartureDate,
        referral.decisionDate,
        uk.gov.justice.digital.hmpps.model.ApprovedPremises(approvedPremises)
    )
}

const val EXT_REF_BOOKING_PREFIX = "urn:uk:gov:hmpps:approved-premises-service:booking:"
const val EXT_REF_REHABILITATIVE_ACTIVITY_PREFIX = "urn:uk:gov:hmpps:approved-premises-service:rehabilitative-activity:"
const val EXT_REF_PREARRIVAL_PREFIX = "urn:uk:gov:hmpps:approved-premises-service:pre-arrival:"