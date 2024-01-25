package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.WorkforceAllocationsClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.service.AllocateEventService
import uk.gov.justice.digital.hmpps.service.AllocatePersonService
import uk.gov.justice.digital.hmpps.service.AllocateRequirementService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {

    @Mock
    internal lateinit var allocationsClient: WorkforceAllocationsClient

    @Mock
    internal lateinit var allocatePersonService: AllocatePersonService

    @Mock
    internal lateinit var allocateEventService: AllocateEventService

    @Mock
    internal lateinit var allocateRequirementService: AllocateRequirementService

    @Mock
    internal lateinit var telemetryService: TelemetryService

    @Mock
    internal lateinit var converter: NotificationConverter<HmppsDomainEvent>

    @InjectMocks
    internal lateinit var handler: Handler

    @Test
    fun `ignorable messages are logged to telemetry`() {
        val crn = "X756391"
        val eventNumber = "12"
        val allocationEvent = mock<HmppsDomainEvent>()
        val allocationDetail = mock<AllocationDetail.EventAllocation>()
        whenever(allocationEvent.personReference).thenReturn(PersonReference(listOf(PersonIdentifier("CRN", crn))))
        whenever(allocationEvent.detailUrl).thenReturn("https://some-url")
        whenever(allocationsClient.getAllocationDetail(any())).thenReturn(allocationDetail)
        whenever(
            allocateEventService.createEventAllocation(
                crn,
                allocationDetail
            )
        ).thenThrow(IgnorableMessageException("Pending transfer exists in Delius", mapOf("eventNumber" to eventNumber)))

        handler.handle(Notification(allocationEvent))

        verify(telemetryService).trackEvent(
            eq("AllocationFailed"),
            eq(
                mapOf(
                    "type" to "EventAllocation",
                    "crn" to crn,
                    "reason" to "Pending transfer exists in Delius",
                    "eventNumber" to eventNumber
                )
            ),
            any()
        )
    }
}