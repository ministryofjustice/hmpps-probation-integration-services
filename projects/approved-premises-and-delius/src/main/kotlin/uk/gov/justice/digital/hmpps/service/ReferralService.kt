package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.datetime.DeliusDateFormatter
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.approvedpremises.*
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.contact.outcome.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.Nsi
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
import uk.gov.justice.digital.hmpps.security.ServiceContext
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
    fun bookingMade(crn: String, details: BookingMade, ap: ApprovedPremises) {
        val person = personRepository.getByCrn(crn)
        val event = eventRepository.getEvent(person.id, details.eventNumber)
        val apTeam = teamRepository.getApprovedPremisesTeam(ap.code.code)
        val apStaff = staffRepository.getUnallocated(apTeam.code)
        val rTeam = teamRepository.getUnallocatedTeam(ap.probationArea.code)
        val rStaff = staffRepository.getByCode(details.bookedBy.staffMember.staffCode)
        val findReferral = {
            referralRepository.findByPersonIdAndCreatedByUserIdAndReferralNotesContains(
                person.id,
                ServiceContext.servicePrincipal()!!.userId,
                Nsi.EXT_REF_BOOKING_PREFIX + details.bookingId
            )
        }
        findReferral() ?: run {
            eventRepository.findForUpdate(event.id)
            findReferral() ?: run {
                referralRepository.save(details.referral(person, event, ap, apTeam, apStaff, rTeam, rStaff))
                contactService.createContact(
                    ContactDetails(
                        date = details.bookingMadeAt,
                        type = ContactTypeCode.BOOKING_MADE,
                        notes = """
                            Expected arrival: ${DeliusDateFormatter.format(details.arrivalOn)}
                            Expected departure: ${DeliusDateFormatter.format(details.departureOn)}
                            
                            To view details of the Approved Premises booking, click here: ${details.applicationUrl}
                            """.trimIndent(),
                        description = "Approved Premises Booking for ${details.premises.name}",
                        locationCode = ap.locationCode()
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
        val referral = getReferral(person, Nsi.EXT_REF_BOOKING_PREFIX + details.bookingId)
        contactService.createContact(
            ContactDetails(
                date = details.changedAt,
                type = ContactTypeCode.BOOKING_CHANGED,
                locationCode = ap.locationCode(),
                description = "Booking changed for ${details.premises.name}",
                notes = listOfNotNull(
                    "The expected arrival and/or departure dates for the booking have changed.",
                    "Previous: ${DeliusDateFormatter.format(referral.expectedArrivalDate)} to ${
                        DeliusDateFormatter.format(
                            referral.expectedDepartureDate
                        )
                    }",
                    "Current: ${DeliusDateFormatter.format(details.arrivalOn)} to ${DeliusDateFormatter.format(details.departureOn)}",
                    "For more details, click here: ${details.applicationUrl}"
                ).joinToString(System.lineSeparator() + System.lineSeparator())
            ),
            person = person,
            eventId = referral.eventId,
            staff = staffRepository.getByCode(details.changedBy.staffCode),
            probationAreaCode = ap.probationArea.code
        )
        referral.apply {
            expectedArrivalDate = details.arrivalOn
            expectedDepartureDate = details.departureOn
            decisionId = referenceDataRepository.acceptedDeferredAdmission().id
        }
    }

    fun bookingCancelled(crn: String, details: BookingCancelled, ap: ApprovedPremises) {
        val person = personRepository.getByCrn(crn)
        val externalReference = Nsi.EXT_REF_BOOKING_PREFIX + details.bookingId
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
                date = details.cancelledAt,
                type = ContactTypeCode.BOOKING_CANCELLED,
                locationCode = ap.locationCode(),
                description = "Booking cancelled for ${details.premises.name}",
                notes = listOfNotNull(
                    details.cancellationReason,
                    "For more details, click here: ${details.applicationUrl}"
                ).joinToString(System.lineSeparator() + System.lineSeparator())
            ),
            person = person,
            eventId = referral?.eventId ?: eventRepository.getEvent(person.id, details.eventNumber).id,
            staff = staffRepository.getByCode(details.cancelledBy.staffCode),
            probationAreaCode = ap.probationArea.code
        )
    }

    fun personNotArrived(person: Person, ap: ApprovedPremises, dateTime: ZonedDateTime, details: PersonNotArrived) {
        val referral = getReferral(person, Nsi.EXT_REF_BOOKING_PREFIX + details.bookingId)
        referral.nonArrivalDate = dateTime.toLocalDate()
        referral.nonArrivalNotes = details.notes
        referral.nonArrivalReasonId =
            referenceDataRepository.findByCodeAndDatasetCode("D", DatasetCode.AP_NON_ARRIVAL_REASON)?.id
        contactService.createContact(
            ContactDetails(
                date = dateTime,
                type = ContactTypeCode.NOT_ARRIVED,
                locationCode = ap.locationCode(),
                description = details.reason,
                outcomeCode = ContactOutcome.AP_NON_ARRIVAL_PREFIX + details.reasonCode,
                notes = listOfNotNull(
                    details.notes,
                    "For more details, click here: ${details.applicationUrl}"
                ).joinToString(System.lineSeparator() + System.lineSeparator())
            ),
            person = person,
            eventId = referral.eventId,
            staff = staffRepository.getByCode(details.recordedBy.staffCode),
            probationAreaCode = ap.probationArea.code
        )
    }

    fun personArrived(person: Person, ap: ApprovedPremises, details: PersonArrived) {
        val referral = getReferral(person, Nsi.EXT_REF_BOOKING_PREFIX + details.bookingId)
        referral.admissionDate = details.arrivedAt.toLocalDate()
        val kw = staffRepository.getByCode(details.keyWorker.staffCode)
        residenceRepository.save(details.residence(person, ap, referral, kw))
    }

    fun personDeparted(person: Person, details: PersonDeparted) {
        val serviceUserId = ServiceContext.servicePrincipal()!!.userId
        val externalReference = Nsi.EXT_REF_BOOKING_PREFIX + details.bookingId
        val referral = getReferral(person, externalReference)
        val residence = residenceRepository.findByReferralIdAndCreatedByUserId(referral.id, serviceUserId)
            ?: throw IgnorableMessageException(
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

    fun findReferral(person: Person, externalReference: String): Referral? =
        referralRepository.findByPersonIdAndCreatedByUserIdAndReferralNotesContains(
            person.id,
            ServiceContext.servicePrincipal()!!.userId,
            externalReference
        )

    fun getReferralDetails(crn: String, bookingId: String): ReferralDetail =
        referralRepository.findReferralDetail(crn, Nsi.EXT_REF_BOOKING_PREFIX + bookingId)?.let {
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
            referralNotes = Nsi.EXT_REF_BOOKING_PREFIX + bookingId + System.lineSeparator() + notes,
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
            referringStaffId = referringStaff.id
        )
    }

    private fun PersonArrived.residence(person: Person, ap: ApprovedPremises, referral: Referral, keyWorker: Staff) =
        Residence(
            person.id,
            referral.id,
            ap.id,
            arrivedAt,
            "This residence is being managed in the AP Referral Service. Please Do NOT make any updates to the record using Delius. Thank you.",
            keyWorker.id
        )

    private fun ReferralWithAp.apReferral() = ApReferral(
        referral.referralDate,
        referral.expectedArrivalDate,
        referral.expectedDepartureDate,
        referral.decisionDate,
        uk.gov.justice.digital.hmpps.model.ApprovedPremises(approvedPremises)
    )
}
