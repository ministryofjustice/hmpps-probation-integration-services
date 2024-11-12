package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.data.generatorimport.HearingOffenceGenerator
import uk.gov.justice.digital.hmpps.data.generatorimport.ProsecutionCaseIdentifierGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
internal class EventServiceTest {

    @Mock
    lateinit var auditedInteractionService: AuditedInteractionService

    @Mock
    lateinit var eventRepository: EventRepository

    @Mock
    lateinit var mainOffenceRepository: MainOffenceRepository

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var courtAppearanceRepository: CourtAppearanceRepository

    @Mock
    lateinit var detailedOffenceRepository: DetailedOffenceRepository

    @Mock
    lateinit var offenceRepository: OffenceRepository

    @Mock
    lateinit var orderManagerRepository: OrderManagerRepository

    @Mock
    lateinit var courtRepository: CourtRepository

    @Mock
    lateinit var teamRepository: TeamRepository

    @Mock
    lateinit var staffRepository: StaffRepository

    @Mock
    lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    lateinit var contactTypeRepository: ContactTypeRepository

    @Mock
    lateinit var transferReasonRepository: TransferReasonRepository

    @InjectMocks
    lateinit var eventService: EventService

    @Test
    fun `insert event saves multiple records`() {

        val person = PersonGenerator.DEFAULT
        val event = EventGenerator.generate(person)
        val orderManager = OrderManagerGenerator.generate(event = event)
        val detailedOffence = DetailedOffenceGenerator.DEFAULT
        val offence = OffenceGenerator.DEFAULT
        val mainOffence = MainOffenceGenerator.generate(event = event, person = person, offence = offence)
        val hearingOffence = HearingOffenceGenerator.generate()
        val court = CourtGenerator.UNKNOWN_COURT_N07_PROVIDER
        val sittingDay = ZonedDateTime.now()
        val caseUrn = ProsecutionCaseIdentifierGenerator.DEFAULT.caseURN

        whenever(eventRepository.save(any())).thenReturn(event)
        whenever(orderManagerRepository.save(any())).thenReturn(orderManager)
        whenever(mainOffenceRepository.save(any())).thenReturn(mainOffence)
        whenever(contactRepository.save(any())).thenReturn(ContactGenerator.EAPP)
        whenever(courtAppearanceRepository.save(any())).thenReturn(
            CourtAppearanceGenerator.generate(
                event = event,
                appearanceType = ReferenceDataGenerator.TRIAL_ADJOURNMENT_APPEARANCE_TYPE,
                court = court,
                person = person
            )
        )

        whenever(eventRepository.getNextEventNumber(person.id!!)).thenReturn(1L.toString())
        whenever(detailedOffenceRepository.findByCode(hearingOffence.offenceCode!!)).thenReturn(detailedOffence)
        whenever(offenceRepository.findByMainOffenceCode(detailedOffence.homeOfficeCode!!)).thenReturn(offence)
        whenever(courtRepository.findByOuCode(court.ouCode!!)).thenReturn(court)
        whenever(teamRepository.findByCode(any())).thenReturn(TeamGenerator.UNALLOCATED)
        whenever(staffRepository.findByCode(any())).thenReturn(StaffGenerator.UNALLOCATED)
        whenever(contactTypeRepository.findByCode(ContactTypeCode.COURT_APPEARANCE.code)).thenReturn(
            ContactTypeGenerator.EAPP
        )
        whenever(transferReasonRepository.findByCode(TransferReason.Reason.CASE_ORDER.code)).thenReturn(
            TransferReasonGenerator.CASE_ORDER
        )
        whenever(
            referenceDataRepository.findByCodeAndDatasetCode(
                ReferenceData.StandardRefDataCode.GUILTY.code,
                DatasetCode.PLEA
            )
        ).thenReturn(ReferenceDataGenerator.GUILTY_PLEA)
        whenever(
            referenceDataRepository.findByCodeAndDatasetCode(
                ReferenceData.StandardRefDataCode.TRIAL_ADJOURNMENT_APPEARANCE.code,
                DatasetCode.COURT_APPEARANCE_TYPE
            )
        ).thenReturn(ReferenceDataGenerator.TRIAL_ADJOURNMENT_APPEARANCE_TYPE)
        whenever(
            referenceDataRepository.findByCodeAndDatasetCode(
                ReferenceData.StandardRefDataCode.REMANDED_IN_CUSTODY_OUTCOME.code,
                DatasetCode.COURT_APPEARANCE_OUTCOME
            )
        ).thenReturn(ReferenceDataGenerator.REMANDED_IN_CUSTODY_OUTCOME)
        whenever(
            referenceDataRepository.findByCodeAndDatasetCode(
                ReferenceData.StandardRefDataCode.REMANDED_IN_CUSTODY_STATUS.code,
                DatasetCode.REMAND_STATUS
            )
        ).thenReturn(ReferenceDataGenerator.REMANDED_IN_CUSTODY_STATUS)
        whenever(
            referenceDataRepository.findByCodeAndDatasetCode(
                ReferenceData.StandardRefDataCode.INITIAL_ALLOCATION.code,
                DatasetCode.ORDER_ALLOCATION_REASON
            )
        ).thenReturn(ReferenceDataGenerator.ORDER_MANAGER_INITIAL_ALLOCATION)

        val result = eventService.insertEvent(hearingOffence, person, court.ouCode!!, sittingDay, caseUrn)

        verify(eventRepository).save(any<Event>())
        verify(mainOffenceRepository).save(any<MainOffence>())
        verify(orderManagerRepository).save(any<OrderManager>())
        verify(contactRepository, times(2)).save(any<Contact>())
        verify(courtAppearanceRepository, times(2)).save(any<CourtAppearance>())

        assertThat(result.event.person.id, equalTo(person.id))
        assertThat(result.mainOffence.person.id, equalTo(person.id))
        assertThat(result.courtAppearances.size, equalTo(2))
        assertThat(result.contacts.size, equalTo(2))
        assertThat(result.orderManager.event.id, equalTo(event.id))
    }
}