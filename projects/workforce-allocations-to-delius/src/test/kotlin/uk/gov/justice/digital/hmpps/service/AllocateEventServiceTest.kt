package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.audit.service.OptimisationTables
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.exception.NotActiveException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exceptions.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocationValidator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReasonCode
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReasonRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.EventAllocation
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

@ExtendWith(MockitoExtension::class)
internal class AllocateEventServiceTest {

    @Mock
    private lateinit var auditedInteractionService: AuditedInteractionService

    @Mock
    private lateinit var eventRepository: EventRepository

    @Mock
    private lateinit var orderManagerRepository: OrderManagerRepository

    @Mock
    private lateinit var allocationValidator: AllocationValidator

    @Mock
    private lateinit var contactTypeRepository: ContactTypeRepository

    @Mock
    private lateinit var contactRepository: ContactRepository

    @Mock
    private lateinit var transferReasonRepository: TransferReasonRepository

    @Mock
    private lateinit var optimisationTables: OptimisationTables

    @InjectMocks
    private lateinit var allocateEventService: AllocateEventService

    private val allocationDetail = ResourceLoader.file<EventAllocation>("get-event-allocation-body")

    @Test
    fun `when event not found exception thrown`() {
        whenever(eventRepository.findByPersonCrnAndNumberAndSoftDeletedFalse(any(), any())).thenReturn(null)

        val ex = assertThrows<NotFoundException> {
            allocateEventService.createEventAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
        assertThat(ex.message, equalTo("Event 1 not found for crn X123456"))
    }

    @Test
    fun `when event not active exception thrown`() {
        whenever(
            eventRepository.findByPersonCrnAndNumberAndSoftDeletedFalse(
                PersonGenerator.DEFAULT.crn,
                allocationDetail.eventNumber.toString()
            )
        )
            .thenReturn(EventGenerator.generate(active = false))

        assertThrows<NotActiveException> {
            allocateEventService.createEventAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }

    @Test
    fun `when order manager not found for event and date exception thrown`() {
        val event = EventGenerator.generate()
        whenever(
            eventRepository.findByPersonCrnAndNumberAndSoftDeletedFalse(
                PersonGenerator.DEFAULT.crn,
                allocationDetail.eventNumber.toString()
            )
        ).thenReturn(event)

        whenever(orderManagerRepository.findActiveManagerAtDate(event.id, allocationDetail.createdDate))
            .thenReturn(null)

        assertThrows<NotFoundException> {
            allocateEventService.createEventAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }

    @Test
    fun `when duplicate allocation noop`() {
        val allocationDetail = allocationDetail.copy(
            staffCode = OrderManagerGenerator.DEFAULT.staff.code,
            teamCode = OrderManagerGenerator.DEFAULT.team.code
        )
        val event = EventGenerator.generate()
        whenever(
            eventRepository.findByPersonCrnAndNumberAndSoftDeletedFalse(
                PersonGenerator.DEFAULT.crn,
                allocationDetail.eventNumber.toString()
            )
        ).thenReturn(event)

        whenever(orderManagerRepository.findActiveManagerAtDate(event.id, allocationDetail.createdDate))
            .thenReturn(OrderManagerGenerator.DEFAULT)

        assertDoesNotThrow {
            allocateEventService.createEventAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
        verify(eventRepository, never()).countPendingTransfers(any())
    }

    @Test
    fun `when pending transfer for event exception thrown`() {
        val event = EventGenerator.generate()
        whenever(
            eventRepository.findByPersonCrnAndNumberAndSoftDeletedFalse(
                PersonGenerator.DEFAULT.crn,
                "1"
            )
        ).thenReturn(event)

        whenever(orderManagerRepository.findActiveManagerAtDate(event.id, allocationDetail.createdDate))
            .thenReturn(OrderManagerGenerator.DEFAULT)

        whenever(eventRepository.countPendingTransfers(event.id)).thenReturn(1)

        assertThrows<IgnorableMessageException> {
            allocateEventService.createEventAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }

    @Test
    fun `when transfer reason not found exception thrown`() {
        val event = EventGenerator.generate()
        whenever(
            eventRepository.findByPersonCrnAndNumberAndSoftDeletedFalse(
                PersonGenerator.DEFAULT.crn,
                allocationDetail.eventNumber.toString()
            )
        ).thenReturn(event)
        whenever(orderManagerRepository.findActiveManagerAtDate(event.id, allocationDetail.createdDate))
            .thenReturn(OrderManagerGenerator.DEFAULT)
        whenever(eventRepository.countPendingTransfers(event.id)).thenReturn(0)
        whenever(transferReasonRepository.findByCode(TransferReasonCode.CASE_ORDER.value)).thenReturn(null)

        assertThrows<NotFoundException> {
            allocateEventService.createEventAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }
}
