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
                    referralDate = hearingOffence.plea?.pleaDate
                        ?: throw IllegalArgumentException("No Plea Date found"), // TODO: Identify event's referral date
                    active = true
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

            // 2. Create (+ identify?) the main offence record from the hearing message
            val savedMainOffence = mainOffenceRepository.save(
                MainOffence(
                    id = null,
                    date = LocalDate.now(), // TODO From the offence -> Wording free text field
                    event = savedEvent,
                    offence = offenceRepository.findOffence(hoOffenceCode),
                    softDeleted = false,
                    count = 1, // TODO Need to identify how offence count is used, in most cases appears to be 1
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
            val plea = when (hearingOffence.plea.pleaValue) {
                "NOT_GUILTY" -> referenceDataRepository.findByCodeAndDatasetCode("N", DatasetCode.PLEA)
                "GUILTY" -> referenceDataRepository.findByCodeAndDatasetCode(
                    ReferenceData.StandardRefDataCode.GUILTY.code,
                    DatasetCode.PLEA
                )

                else -> referenceDataRepository.findByCodeAndDatasetCode("Q", DatasetCode.PLEA)
            }

            // 3. Create (two) initial court appearance(s) for the event
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
                    court = court, // TODO: Is the 'next court' always the same as the hearing's court?
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
                    startTime = sittingDay, // TODO Clear the date aspect to 01-Jan-70 for delius formatting?
                    endTime = null,
                    staff = unallocatedStaff,
                    team = unallocatedTeam,
                    alert = true,
                    eventId = savedEvent.id!!,
                    type = courtAppearanceContactType,
                    staffEmployeeId = unallocatedStaff.id,
                    probationAreaId = court.provider.id,
                    trustProviderTeamId = unallocatedTeam.id
                )
            )

            val futureAppearanceContact = contactRepository.save(
                Contact(
                    id = null,
                    date = sittingDay.toLocalDate(), // TODO: Identify the next appearance date from hearing
                    person = person,
                    startTime = sittingDay, // TODO: Identify the next appearance time from hearing
                    endTime = null,
                    alert = true,
                    eventId = savedEvent.id,
                    type = courtAppearanceContactType,
                    staffEmployeeId = unallocatedStaff.id,
                    probationAreaId = court.provider.id,
                    trustProviderTeamId = unallocatedTeam.id
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
            // TODO 6. Update person level remand status field

            audit["offenderId"] = savedEvent.person.id!!
            audit["eventId"] = savedEvent.id
            InsertEventResult(savedEvent, savedMainOffence, savedCourtAppearances, savedContacts, savedOrderManager)
        }
}