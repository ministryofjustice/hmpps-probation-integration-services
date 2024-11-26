package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.messaging.HearingOffence
import java.time.LocalDate
import java.time.ZonedDateTime

@Service
class EventService(
    auditedInteractionService: AuditedInteractionService,
    private val eventRepository: EventRepository,
    private val mainOffenceRepository: MainOffenceRepository,
    private val detailedOffenceRepository: DetailedOffenceRepository,
    private val offenceRepository: OffenceRepository,
    private val orderManagerRepository: OrderManagerRepository,
    private val courtRepository: CourtRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val transferReasonRepository: TransferReasonRepository,
    private val contactRepository: ContactRepository,
    private val courtAppearanceRepository: CourtAppearanceRepository
) : AuditableService(auditedInteractionService) {

    @Transactional
    fun insertEvent(
        hearingOffence: HearingOffence,
        person: Person,
        courtCode: String,
        sittingDay: ZonedDateTime,
        caseUrn: String
    ): InsertEventResult =
        audit(BusinessInteractionCode.INSERT_EVENT) { audit ->

            // 1. Create and save the event entity
            val savedEvent = eventRepository.save(
                Event(
                    id = null,
                    person = person,
                    number = eventRepository.getNextEventNumber(person.id!!),
                    referralDate = sittingDay.toLocalDate(), // TODO: Identify event's referral date
                    active = true,
                    ftcCount = 0
                )
            )

            val hoOffenceCode =
                hearingOffence.offenceCode?.let {
                    detailedOffenceRepository.findByCode(it)?.homeOfficeCode?.replace(
                        "/",
                        ""
                    )
                }
                    ?: throw IllegalArgumentException("Home Office Code cannot be null")

            // 2. Create the main offence record from the hearing message
            val savedMainOffence = mainOffenceRepository.save(
                MainOffence(
                    id = null,
                    date = LocalDate.now(), // TODO: From the offence -> Wording free text field
                    event = savedEvent,
                    offence = offenceRepository.findOffence(hoOffenceCode),
                    softDeleted = false,
                    count = 1, // TODO: Need to identify how offence count is used, in most cases appears to be 1
                    person = person,
                )
            )

            val court = courtRepository.getByOuCode(courtCode)
            val courtAppearanceContactType = contactTypeRepository.getByCode(ContactTypeCode.COURT_APPEARANCE.code)
            val unallocatedTeam = teamRepository.findByCode(court.provider.code + "UAT")
            val unallocatedStaff = staffRepository.findByCode(unallocatedTeam.code + "U")
            val trialAdjournmentRefData = referenceDataRepository.trialAdjournmentAppearanceType()
            val remandedInCustodyOutcome = referenceDataRepository.remandedInCustodyOutcome()
            val remandedInCustodyStatus = referenceDataRepository.remandedInCustodyStatus()
            val plea = when (hearingOffence.plea?.pleaValue) {
                "NOT_GUILTY" -> referenceDataRepository.findByCodeAndDatasetCode(
                    ReferenceData.StandardRefDataCode.NOT_GUILTY.code,
                    DatasetCode.PLEA
                )

                "GUILTY" -> referenceDataRepository.findByCodeAndDatasetCode(
                    ReferenceData.StandardRefDataCode.GUILTY.code,
                    DatasetCode.PLEA
                )

                else -> referenceDataRepository.findByCodeAndDatasetCode(
                    ReferenceData.StandardRefDataCode.NOT_KNOWN_PLEA.code,
                    DatasetCode.PLEA
                )
            }

            // 3. Create initial court appearances for the event
            val initialCourtAppearance = courtAppearanceRepository.save(
                CourtAppearance(
                    id = null,
                    appearanceDate = sittingDay.toLocalDate(),
                    crownCourtCalendarNumber = caseUrn,
                    event = savedEvent,
                    teamId = unallocatedTeam.id,
                    staffId = unallocatedStaff.id,
                    softDeleted = false,
                    court = court,
                    appearanceType = trialAdjournmentRefData, // TODO: Are all initial hearing messages pre-sentence events Trial / Adjournment?
                    plea = plea,
                    outcome = remandedInCustodyOutcome, // TODO: Determine the outcome, for now use Remanded in custody
                    remandStatus = remandedInCustodyStatus,
                    person = person
                )
            )

            val futureCourtAppearance = courtAppearanceRepository.save(
                CourtAppearance(
                    id = null,
                    appearanceDate = sittingDay.toLocalDate(), // TODO: Identify the 2nd (future) court appearance date
                    crownCourtCalendarNumber = caseUrn,
                    event = savedEvent,
                    teamId = unallocatedTeam.id,
                    staffId = unallocatedStaff.id,
                    softDeleted = false,
                    court = court, // TODO: How do we identify the 'next court', for now use the same court
                    appearanceType = trialAdjournmentRefData,
                    person = person
                )
            )

            val savedCourtAppearances = listOf(initialCourtAppearance, futureCourtAppearance)

            // 4. Create an initial contact for each court appearance
            val initialContact = contactRepository.save(
                Contact(
                    id = null,
                    date = sittingDay.toLocalDate(),
                    person = person,
                    startTime = sittingDay.with(LocalDate.of(1970, 1, 1)),
                    endTime = null,
                    alert = true,
                    eventId = savedEvent.id!!,
                    type = courtAppearanceContactType,
                    probationAreaId = court.provider.id,
                    team = unallocatedTeam,
                    trustProviderTeamId = unallocatedTeam.id,
                    staff = unallocatedStaff,
                    staffEmployeeId = unallocatedStaff.id
                )
            )

            val futureAppearanceContact = contactRepository.save(
                Contact(
                    id = null,
                    date = futureCourtAppearance.appearanceDate,
                    person = person,
                    startTime = sittingDay.with(LocalDate.of(1970, 1, 1)),
                    endTime = null,
                    alert = true,
                    eventId = savedEvent.id,
                    type = courtAppearanceContactType,
                    probationAreaId = court.provider.id,
                    team = unallocatedTeam,
                    trustProviderTeamId = unallocatedTeam.id,
                    staff = unallocatedStaff,
                    staffEmployeeId = unallocatedStaff.id
                )
            )

            val savedContacts = listOf(initialContact, futureAppearanceContact)

            // 5. Create order manager record
            val savedOrderManager = orderManagerRepository.save(
                OrderManager(
                    id = null,
                    allocationDate = savedEvent.referralDate,
                    teamId = unallocatedTeam.id,
                    staffId = unallocatedStaff.id,
                    softDeleted = false,
                    endDate = null,
                    event = savedEvent,
                    allocationReason = referenceDataRepository.initialOrderAllocationReason(),
                    providerEmployeeId = null,
                    providerTeamId = null,
                    transferReason = transferReasonRepository.caseOrderTransferReason(),
                    staffEmployeeId = unallocatedStaff.id,
                    trustProviderTeamId = unallocatedTeam.id,
                    providerId = court.provider.id,
                    active = true
                )
            )
            // TODO 6. Update person level remand status field?

            audit["offenderId"] = savedEvent.person.id!!
            audit["eventId"] = savedEvent.id
            InsertEventResult(savedEvent, savedMainOffence, savedCourtAppearances, savedContacts, savedOrderManager)
        }

    @Transactional
    fun insertCourtAppearance(
        event: Event,
        courtCode: String,
        sittingDay: ZonedDateTime,
        caseUrn: String
    ): CourtAppearance =
        audit(BusinessInteractionCode.INSERT_COURT_APPEARANCE) { audit ->
            val court = courtRepository.getByOuCode(courtCode)
            val unallocatedTeam = teamRepository.findByCode(court.provider.code + "UAT")
            val unallocatedStaff = staffRepository.findByCode(unallocatedTeam.code + "U")
            val trialAdjournmentRefData = referenceDataRepository.trialAdjournmentAppearanceType()

            val savedCourtAppearance = courtAppearanceRepository.save(
                CourtAppearance(
                    id = null,
                    appearanceDate = sittingDay.toLocalDate(),
                    crownCourtCalendarNumber = caseUrn,
                    event = event,
                    teamId = unallocatedTeam.id,
                    staffId = unallocatedStaff.id,
                    softDeleted = false,
                    court = court,
                    appearanceType = trialAdjournmentRefData,
                    person = event.person
                )
            )

            audit["courtAppearanceId"] = savedCourtAppearance.id!!
            savedCourtAppearance
        }
}