package uk.gov.justice.digital.hmpps.integrations.delius.offender

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.RegisterType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.service.DomainEventService
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class OffenderDeltaServiceTest {

    @Mock
    lateinit var offenderDeltaRepository: OffenderDeltaRepository

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var notificationPublisher: NotificationPublisher

    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var domainEventService: DomainEventService

    @Mock
    lateinit var registrationRepository: RegistrationRepository

    private lateinit var offenderDeltaService: OffenderDeltaService

    @BeforeEach
    fun setUp() {
        offenderDeltaService = OffenderDeltaService(
            batchSize = 5,
            offenderDeltaRepository = offenderDeltaRepository,
            contactRepository = contactRepository,
            notificationPublisher = notificationPublisher,
            telemetryService = telemetryService,
            domainEventService = domainEventService,
            registrationRepository = registrationRepository,
        )
    }

    @ParameterizedTest
    @MethodSource("nonContactTables")
    fun `when sourceTable is not CONTACT then no domain event is generated`(table: String) {
        // given
        val offender = Offender(
            id = id(),
            crn = "X999999",
            nomsNumber = null
        )

        val occurredAt = ZonedDateTime.parse("2025-12-15T17:30:30Z")

        val delta = OffenderDelta(
            id = id(),
            offender = offender,
            sourceTable = table,
            sourceRecordId = id(),
            action = "UPSERT",
            dateChanged = occurredAt
        )

        // when
        offenderDeltaService.notify(delta)

        // then — handleDomainEvent is NOT triggered
        verify(domainEventService, never())
            .publishContactUpdated(any(), any(), any(), any())

        verify(domainEventService, never())
            .publishContactDeleted(any(), any(), any(), any())
    }

    @ParameterizedTest
    @MethodSource("singleInteractionTables")
    fun `when non-CONTACT sourceTable has single interaction then exactly one notification and telemetry is produced`(
        table: String
    ) {
        // given
        val offender = Offender(
            id = id(),
            crn = "X101010",
            nomsNumber = null
        )

        val occurredAt = ZonedDateTime.parse("2025-12-15T18:00:00Z")

        val delta = OffenderDelta(
            id = id(),
            offender = offender,
            sourceTable = table,
            sourceRecordId = id(),
            action = "UPSERT",
            dateChanged = occurredAt
        )

        // when
        offenderDeltaService.notify(delta)

        // then — no domain events
        verify(domainEventService, never())
            .publishContactUpdated(any(), any(), any(), any())

        verify(domainEventService, never())
            .publishContactDeleted(any(), any(), any(), any())

        // then — exactly one notification
        val notificationCaptor = argumentCaptor<Notification<*>>()
        verify(notificationPublisher, times(1))
            .publish(notificationCaptor.capture())

        // then — exactly one telemetry event
        verify(telemetryService, times(1))
            .trackEvent(any(), any(), any())

        // then — notification must have a valid eventType
        val eventType = notificationCaptor.firstValue.eventType
        requireNotNull(eventType)

        Assertions.assertTrue(
            eventType.endsWith("_CHANGED")
                || eventType.endsWith("_DELETED")
                || eventType.endsWith("_DEREGISTERED")
                || eventType == "OFFENDER_MERGED",
            "Unexpected eventType for table=$table : $eventType"
        )
    }

    @ParameterizedTest
    @MethodSource("multipleInteractionTables")
    fun `when non-CONTACT sourceTable has multiple interactions then two notifications and two telemetry events are produced`(
        table: String
    ) {
        // given
        val offender = Offender(
            id = id(),
            crn = "X202202",
            nomsNumber = null
        )

        val occurredAt = ZonedDateTime.parse("2025-12-15T18:30:00Z")

        val delta = OffenderDelta(
            id = id(),
            offender = offender,
            sourceTable = table,
            sourceRecordId = id(),
            action = "UPSERT",
            dateChanged = occurredAt
        )

        // when
        offenderDeltaService.notify(delta)

        // then — no domain events
        verify(domainEventService, never())
            .publishContactUpdated(any(), any(), any(), any())

        verify(domainEventService, never())
            .publishContactDeleted(any(), any(), any(), any())

        // then — exactly two notifications
        val notificationCaptor = argumentCaptor<Notification<*>>()
        verify(notificationPublisher, times(2))
            .publish(notificationCaptor.capture())

        val eventTypes = notificationCaptor.allValues
            .mapNotNull { it.eventType }
            .toSet()

        // must contain OFFENDER_CHANGED
        Assertions.assertTrue(
            eventTypes.contains("OFFENDER_CHANGED"),
            "Expected OFFENDER_CHANGED notification for table=$table but got $eventTypes"
        )

        // must contain one specific event (not OFFENDER_CHANGED)
        Assertions.assertEquals(
            2,
            eventTypes.size,
            "Expected exactly two distinct eventTypes for table=$table but got $eventTypes"
        )

        // then — exactly two telemetry events
        verify(telemetryService, times(2))
            .trackEvent(any(), any(), any())
    }

    @Test
    fun `when sourceTable is MANAGEMENT_TIER_EVENT then no notification telemetry or domain event is generated`() {
        // given
        val offender = Offender(
            id = id(),
            crn = "X999999",
            nomsNumber = null
        )

        val occurredAt = ZonedDateTime.parse("2025-12-15T17:31:30Z")

        val delta = OffenderDelta(
            id = id(),
            offender = offender,
            sourceTable = "MANAGEMENT_TIER_EVENT",
            sourceRecordId = id(),
            action = "UPSERT",
            dateChanged = occurredAt
        )

        // when
        offenderDeltaService.notify(delta)

        // then — NO NOTIFICATIONS
        verify(notificationPublisher, never())
            .publish(any())

        // then — NO DOMAIN EVENTS
        verify(domainEventService, never())
            .publishContactUpdated(any(), any(), any(), any())

        verify(domainEventService, never())
            .publishContactDeleted(any(), any(), any(), any())

        // then — NO TELEMETRY
        verify(telemetryService, never())
            .trackEvent(any(), any(), any())
    }

    @Test
    fun `when CONTACT but offender is null then no domain event is generated`() {
        // given
        val occurredAt = ZonedDateTime.parse("2025-12-15T17:24:30Z")

        val delta = OffenderDelta(
            id = id(),
            offender = null,
            sourceTable = "CONTACT",
            sourceRecordId = id(),
            action = "UPSERT",
            dateChanged = occurredAt
        )

        // when
        offenderDeltaService.notify(delta)

        // then — DOMAIN EVENT is NOT published
        verify(domainEventService, never())
            .publishContactUpdated(any(), any(), any(), any())

        verify(domainEventService, never())
            .publishContactDeleted(any(), any(), any(), any())

        // then — NO DATA EVENT notification
        verify(notificationPublisher, never())
            .publish(any())

        // then — NO TELEMETRY
        verify(telemetryService, never())
            .trackEvent(any(), any(), any())
    }

    @Test
    fun `when CONTACT and contact record does not exist then no domain event is generated`() {
        // given
        val offender = Offender(
            id = id(),
            crn = "X12345",
            nomsNumber = null
        )

        val occurredAt = ZonedDateTime.parse("2025-12-15T17:25:30Z")
        val sourceRecordId = id()

        val delta = OffenderDelta(
            id = id(),
            offender = offender,
            sourceTable = "CONTACT",
            sourceRecordId = sourceRecordId,
            action = "UPSERT",
            dateChanged = occurredAt
        )

        // contact does not exist
        whenever(contactRepository.existsByIdAndVisorContactTrue(sourceRecordId))
            .thenReturn(false)

        whenever(contactRepository.existsByIdAndSoftDeletedFalse(sourceRecordId))
            .thenReturn(false)

        // when
        offenderDeltaService.notify(delta)

        // then — DOMAIN EVENT is NOT published
        verify(domainEventService, never())
            .publishContactUpdated(any(), any(), any(), any())

        verify(domainEventService, never())
            .publishContactDeleted(any(), any(), any(), any())

        // then — DATA EVENT notification is published
        val captor = argumentCaptor<Notification<*>>()
        verify(notificationPublisher).publish(captor.capture())

        Assertions.assertEquals(
            "CONTACT_DELETED",
            captor.firstValue.eventType
        )

        // then — TELEMETRY is recorded
        verify(telemetryService)
            .trackEvent(any(), any(), any())
    }

    @Test
    fun `when CONTACT and VISOR flag is false then no domain event is generated`() {
        // given
        val offender = Offender(
            id = id(),
            crn = "X303030",
            nomsNumber = null
        )

        val occurredAt = ZonedDateTime.parse("2025-12-15T17:27:30Z")
        val sourceRecordId = id()

        val delta = OffenderDelta(
            id = id(),
            offender = offender,
            sourceTable = "CONTACT",
            sourceRecordId = sourceRecordId,
            action = "UPSERT",
            dateChanged = occurredAt
        )

        whenever(contactRepository.existsByIdAndVisorContactTrue(sourceRecordId))
            .thenReturn(false)

        whenever(contactRepository.existsByIdAndSoftDeletedFalse(sourceRecordId))
            .thenReturn(false)

        // when
        offenderDeltaService.notify(delta)

        // then — NO DOMAIN EVENTS
        verify(domainEventService, never())
            .publishContactUpdated(any(), any(), any(), any())

        verify(domainEventService, never())
            .publishContactDeleted(any(), any(), any(), any())

        // then — DATA EVENT notification is published
        verify(notificationPublisher)
            .publish(any())

        // then — TELEMETRY is recorded
        verify(telemetryService)
            .trackEvent(any(), any(), any())
    }

    @Test
    fun `when CONTACT_CHANGED and VISOR flag is true but no MAPPA registration exists then category defaults to 0`() {
        // given
        val offender = Offender(
            id = id(),
            crn = "X202020",
            nomsNumber = null
        )

        val occurredAt = ZonedDateTime.parse("2025-12-15T17:26:30Z")
        val sourceRecordId = id()

        val delta = OffenderDelta(
            id = id(),
            offender = offender,
            sourceTable = "CONTACT",
            sourceRecordId = sourceRecordId,
            action = "UPSERT",
            dateChanged = occurredAt
        )

        whenever(contactRepository.existsByIdAndVisorContactTrue(sourceRecordId))
            .thenReturn(true)

        whenever(contactRepository.existsByIdAndSoftDeletedFalse(sourceRecordId))
            .thenReturn(true)

        whenever(
            registrationRepository.findByPersonIdAndTypeCodeOrderByIdDesc(
                offender.id,
                RegisterType.Code.MAPPA.value
            )
        ).thenReturn(emptyList())

        // when
        offenderDeltaService.notify(delta)

        // then — DOMAIN EVENT
        verify(domainEventService).publishContactUpdated(
            crn = eq("X202020"),
            contactId = eq(sourceRecordId),
            category = eq(0),
            occurredAt = eq(occurredAt)
        )

        // then — NOTIFICATION
        val notificationCaptor = argumentCaptor<Notification<*>>()
        verify(notificationPublisher).publish(notificationCaptor.capture())
        Assertions.assertEquals(
            "CONTACT_CHANGED",
            notificationCaptor.firstValue.eventType
        )

        // then — TELEMETRY
        verify(telemetryService, atLeastOnce())
            .trackEvent(any(), any(), any())
    }

    @Test
    fun `when CONTACT_CHANGED and VISOR flag is true then contact updated domain event is generated`() {
        // given
        val offender = Offender(
            id = id(),
            crn = "X111111",
            nomsNumber = null
        )

        val occurredAt = ZonedDateTime.parse("2025-12-15T17:23:30Z")
        val sourceRecordId = id()

        val delta = OffenderDelta(
            id = id(),
            offender = offender,
            sourceTable = "CONTACT",
            sourceRecordId = sourceRecordId,
            action = "UPSERT",
            dateChanged = occurredAt
        )

        // contact exists with ViSOR enabled
        whenever(contactRepository.existsByIdAndVisorContactTrue(sourceRecordId))
            .thenReturn(true)

        whenever(contactRepository.existsByIdAndSoftDeletedFalse(sourceRecordId))
            .thenReturn(true)

        // MAPPA registration with category M4 → 4
        val mappaCategory = ReferenceData(
            code = "M4",
            description = "MAPPA category 4",
            datasetId = id(),
            id = id()
        )

        val registration = mock<Registration> {
            on { category } doReturn mappaCategory
        }

        whenever(
            registrationRepository.findByPersonIdAndTypeCodeOrderByIdDesc(
                offender.id,
                RegisterType.Code.MAPPA.value
            )
        ).thenReturn(listOf(registration))

        // when
        offenderDeltaService.notify(delta)

        // then — DOMAIN EVENT is published
        verify(domainEventService).publishContactUpdated(
            crn = eq("X111111"),
            contactId = eq(sourceRecordId),
            category = eq(4),
            occurredAt = eq(occurredAt)
        )

        // then — DATA EVENT notification is published
        val notificationCaptor = argumentCaptor<Notification<*>>()
        verify(notificationPublisher).publish(notificationCaptor.capture())

        Assertions.assertEquals(
            "CONTACT_CHANGED",
            notificationCaptor.firstValue.eventType
        )

        // then — TELEMETRY is recorded
        verify(telemetryService, atLeastOnce())
            .trackEvent(any(), any(), any())
    }

    /**
     * NOTE:
     *
     * In this scenario the contact record exists but is soft deleted.
     *
     * As a result:
     * - The domain event is still published as CONTACT_UPDATED because the delta action is UPSERT
     *   and the VISOR flag is enabled.
     * - The data event notification is published as CONTACT_DELETED because the contact is no longer
     *   active (existsByIdAndSoftDeletedFalse = false).
     *
     * This difference between domain event semantics and data event semantics is intentional and
     * reflects current business behaviour.
     */
    @Test
    fun `when CONTACT_CHANGED and VISOR is true then contact updated domain event is generated`() {
        // given
        val offender = Offender(
            id = id(),
            crn = "X606060",
            nomsNumber = null
        )

        val occurredAt = ZonedDateTime.parse("2025-12-15T17:28:30Z")
        val sourceRecordId = id()

        val delta = OffenderDelta(
            id = id(),
            offender = offender,
            sourceTable = "CONTACT",
            sourceRecordId = sourceRecordId,
            action = "UPSERT",
            dateChanged = occurredAt
        )

        whenever(contactRepository.existsByIdAndVisorContactTrue(sourceRecordId))
            .thenReturn(true)

        // when
        offenderDeltaService.notify(delta)

        // then — DOMAIN EVENT (UPDATED)
        verify(domainEventService).publishContactUpdated(
            crn = eq("X606060"),
            contactId = eq(sourceRecordId),
            category = eq(0),
            occurredAt = eq(occurredAt)
        )

        // then — NOTIFICATION
        val notificationCaptor = argumentCaptor<Notification<*>>()
        verify(notificationPublisher).publish(notificationCaptor.capture())
        Assertions.assertEquals(
            "CONTACT_DELETED",
            notificationCaptor.firstValue.eventType
        )

        // then — TELEMETRY
        verify(telemetryService, atLeastOnce())
            .trackEvent(any(), any(), any())
    }

    @Test
    fun `when CONTACT_DELETED and VISOR flag is true then contact deleted domain event is generated`() {
        // given
        val offender = Offender(
            id = id(),
            crn = "X111111",
            nomsNumber = null
        )

        val occurredAt = ZonedDateTime.parse("2025-12-15T17:21:30Z")
        val sourceRecordId = id()

        val delta = OffenderDelta(
            id = id(),
            offender = offender,
            sourceTable = "CONTACT",
            sourceRecordId = sourceRecordId,
            action = "DELETE",
            dateChanged = occurredAt
        )

        // contact soft deleted with ViSOR enabled
        whenever(contactRepository.existsByIdAndVisorContactTrue(sourceRecordId))
            .thenReturn(true)

        whenever(contactRepository.existsByIdAndSoftDeletedFalse(sourceRecordId))
            .thenReturn(false)

        val mappaCategory = ReferenceData(
            code = "M4",
            description = "MAPPA category 4",
            datasetId = id(),
            id = id()
        )

        val registration = mock<Registration> {
            on { category } doReturn mappaCategory
        }

        whenever(
            registrationRepository.findByPersonIdAndTypeCodeOrderByIdDesc(
                offender.id,
                RegisterType.Code.MAPPA.value
            )
        ).thenReturn(listOf(registration))

        // when
        offenderDeltaService.notify(delta)

        // then — DOMAIN EVENT is published
        verify(domainEventService).publishContactDeleted(
            crn = eq("X111111"),
            contactId = eq(sourceRecordId),
            category = eq(4),
            occurredAt = eq(occurredAt)
        )

        // then — DATA EVENT notification is published
        val notificationCaptor = argumentCaptor<Notification<*>>()
        verify(notificationPublisher).publish(notificationCaptor.capture())

        Assertions.assertEquals(
            "CONTACT_DELETED",
            notificationCaptor.firstValue.eventType
        )

        // then — TELEMETRY is recorded
        verify(telemetryService, atLeastOnce())
            .trackEvent(any(), any(), any())
    }

    /**
     * NOTE – Interim solution
     *
     * In hard delete scenarios the contact record no longer exists in Delius,
     * therefore the VISOR flag cannot be resolved.
     *
     * As a result, no domain event is generated for CONTACT_DELETED when the
     * contact record is missing.
     *
     * This behaviour is intentional for now and represents an interim solution.
     * It will be revisited and potentially revised as part of a future improvement
     * once a reliable way to resolve VISOR state for hard-deleted contacts is agreed.
     */
    @Test
    fun `when CONTACT_DELETED and contact record does not exist then no domain event is generated because VISOR flag cannot be resolved`() {
        // given
        val offender = Offender(
            id = id(),
            crn = "X808080",
            nomsNumber = null
        )

        val occurredAt = ZonedDateTime.parse("2025-12-15T17:30:30Z")
        val sourceRecordId = id()

        val delta = OffenderDelta(
            id = id(),
            offender = offender,
            sourceTable = "CONTACT",
            sourceRecordId = sourceRecordId,
            action = "DELETE",
            dateChanged = occurredAt
        )

        // hard deleted contact – record does not exist
        whenever(contactRepository.existsByIdAndVisorContactTrue(sourceRecordId))
            .thenReturn(false)

        whenever(contactRepository.existsByIdAndSoftDeletedFalse(sourceRecordId))
            .thenReturn(false)

        // when
        offenderDeltaService.notify(delta)

        // then — NO DOMAIN EVENTS (VISOR flag cannot be resolved)
        verify(domainEventService, never())
            .publishContactUpdated(any(), any(), any(), any())

        verify(domainEventService, never())
            .publishContactDeleted(any(), any(), any(), any())

        // then — NOTIFICATION is published
        val notificationCaptor = argumentCaptor<Notification<*>>()
        verify(notificationPublisher).publish(notificationCaptor.capture())

        Assertions.assertEquals(
            "CONTACT_DELETED",
            notificationCaptor.firstValue.eventType
        )

        // then — TELEMETRY is recorded
        verify(telemetryService)
            .trackEvent(any(), any(), any())
    }

    companion object {
        @JvmStatic
        fun nonContactTables() = listOf(
            "ALIAS",
            "EVENT",
            "COURT_APPEARANCE",
            "OFFENDER_ADDRESS",
            "OFFENDER",
            "OFFENDER_MANAGER",
            "OFFENDER_OFFICER",
            "OGRS_ASSESSMENT",
            "REGISTRATION",
            "DEREGISTRATION",
            "RQMNT",
            "MERGE_HISTORY"
        )

        @JvmStatic
        fun singleInteractionTables() = listOf(
            "EVENT",
            "COURT_APPEARANCE",
            "OGRS_ASSESSMENT",
            "REGISTRATION",
            "DEREGISTRATION",
            "RQMNT",
            "MERGE_HISTORY",
            "OFFENDER_OFFICER"
        )

        @JvmStatic
        fun multipleInteractionTables() = listOf(
            "ALIAS",
            "OFFENDER",
            "OFFENDER_MANAGER",
            "OFFENDER_ADDRESS",
            "OFFICER"
        )
    }
}