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
    fun `when sourceTable is not CONTACT then no domain event is published`(table: String) {
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
        offenderDeltaService.prepare(delta)

        // then — domain events are NOT published
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
        val notifications = offenderDeltaService.prepare(delta)

        // then — exactly one notification prepared
        Assertions.assertEquals(
            1,
            notifications.size,
            "Expected exactly one notification for table=$table"
        )

        // when — NOTIFY
        notifications.forEach(offenderDeltaService::notify)

        // then — no domain events published
        verify(domainEventService, never())
            .publishContactUpdated(any(), any(), any(), any())

        verify(domainEventService, never())
            .publishContactDeleted(any(), any(), any(), any())

        // then — exactly one notification published
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
        val notifications = offenderDeltaService.prepare(delta)

        // then — exactly two notifications prepared
        Assertions.assertEquals(
            2,
            notifications.size,
            "Expected exactly two notifications for table=$table"
        )

        // when — NOTIFY
        notifications.forEach(offenderDeltaService::notify)

        // then — no domain events published
        verify(domainEventService, never())
            .publishContactUpdated(any(), any(), any(), any())

        verify(domainEventService, never())
            .publishContactDeleted(any(), any(), any(), any())

        // then — exactly two notifications published
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

        // must contain exactly one additional event type
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
        val notifications = offenderDeltaService.prepare(delta)

        // then — no notifications prepared
        Assertions.assertTrue(notifications.isEmpty())

        // when — NOTIFY (should effectively do nothing)
        notifications.forEach(offenderDeltaService::notify)

        // then — NO NOTIFICATIONS published
        verify(notificationPublisher, never())
            .publish(any())

        // then — NO DOMAIN EVENTS published
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
        val notifications = offenderDeltaService.prepare(delta)

        // then — no notifications prepared
        Assertions.assertTrue(notifications.isEmpty())

        // when — NOTIFY (should do nothing)
        notifications.forEach(offenderDeltaService::notify)

        // then — DOMAIN EVENTS are NOT published
        verify(domainEventService, never())
            .publishContactUpdated(any(), any(), any(), any())

        verify(domainEventService, never())
            .publishContactDeleted(any(), any(), any(), any())

        // then — NO NOTIFICATIONS published
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
        val notifications = offenderDeltaService.prepare(delta)

        // then — exactly one notification prepared
        Assertions.assertEquals(1, notifications.size)

        Assertions.assertEquals(
            "CONTACT_DELETED",
            notifications.first().eventType
        )

        // when — NOTIFY
        notifications.forEach(offenderDeltaService::notify)

        // then — DOMAIN EVENTS are NOT published
        verify(domainEventService, never())
            .publishContactUpdated(any(), any(), any(), any())

        verify(domainEventService, never())
            .publishContactDeleted(any(), any(), any(), any())

        // then — DATA EVENT notification is published
        val captor = argumentCaptor<Notification<*>>()
        verify(notificationPublisher, times(1))
            .publish(captor.capture())

        Assertions.assertEquals(
            "CONTACT_DELETED",
            captor.firstValue.eventType
        )

        // then — TELEMETRY is recorded
        verify(telemetryService, times(1))
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

        // VISOR flag is false
        whenever(contactRepository.existsByIdAndVisorContactTrue(sourceRecordId))
            .thenReturn(false)

        whenever(contactRepository.existsByIdAndSoftDeletedFalse(sourceRecordId))
            .thenReturn(false)

        // when
        val notifications = offenderDeltaService.prepare(delta)

        // then — at least one notification prepared
        Assertions.assertTrue(notifications.isNotEmpty())

        // when — NOTIFY
        notifications.forEach(offenderDeltaService::notify)

        // then — NO DOMAIN EVENTS published
        verify(domainEventService, never())
            .publishContactUpdated(any(), any(), any(), any())

        verify(domainEventService, never())
            .publishContactDeleted(any(), any(), any(), any())

        // then — DATA EVENT notification is published
        verify(notificationPublisher, times(notifications.size))
            .publish(any())

        // then — TELEMETRY is recorded
        verify(telemetryService, times(notifications.size))
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
        val notifications = offenderDeltaService.prepare(delta)

        // then — exactly one notification prepared
        Assertions.assertEquals(1, notifications.size)

        Assertions.assertEquals(
            "CONTACT_CHANGED",
            notifications.first().eventType
        )

        // when — NOTIFY
        notifications.forEach(offenderDeltaService::notify)

        // then — DOMAIN EVENT published
        verify(domainEventService).publishContactUpdated(
            crn = eq("X202020"),
            contactId = eq(sourceRecordId),
            category = eq(0),
            occurredAt = eq(occurredAt)
        )

        // then — NOTIFICATION published
        val notificationCaptor = argumentCaptor<Notification<*>>()
        verify(notificationPublisher, times(1))
            .publish(notificationCaptor.capture())

        Assertions.assertEquals(
            "CONTACT_CHANGED",
            notificationCaptor.firstValue.eventType
        )

        // then — TELEMETRY
        verify(telemetryService, times(1))
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
        val notifications = offenderDeltaService.prepare(delta)

        // then — exactly one notification prepared
        Assertions.assertEquals(1, notifications.size)

        Assertions.assertEquals(
            "CONTACT_CHANGED",
            notifications.first().eventType
        )

        // when — NOTIFY
        notifications.forEach(offenderDeltaService::notify)

        // then — DOMAIN EVENT is published
        verify(domainEventService).publishContactUpdated(
            crn = eq("X111111"),
            contactId = eq(sourceRecordId),
            category = eq(4),
            occurredAt = eq(occurredAt)
        )

        // then — DATA EVENT notification is published
        val notificationCaptor = argumentCaptor<Notification<*>>()
        verify(notificationPublisher, times(1))
            .publish(notificationCaptor.capture())

        Assertions.assertEquals(
            "CONTACT_CHANGED",
            notificationCaptor.firstValue.eventType
        )

        // then — TELEMETRY is recorded
        verify(telemetryService, times(1))
            .trackEvent(any(), any(), any())
    }

    @Test
    fun `when CONTACT SOFT DELETED and VISOR is true then contact deleted domain event is generated`() {
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

        // soft-deleted contact
        whenever(contactRepository.existsByIdAndSoftDeletedFalse(sourceRecordId))
            .thenReturn(false)

        // when
        val notifications = offenderDeltaService.prepare(delta)

        // then — exactly one notification prepared
        Assertions.assertEquals(1, notifications.size)
        Assertions.assertEquals(
            "CONTACT_DELETED",
            notifications.first().eventType
        )

        // when — NOTIFY
        notifications.forEach(offenderDeltaService::notify)

        // then — DOMAIN EVENT (DELETED) is published
        verify(domainEventService).publishContactDeleted(
            crn = eq("X606060"),
            contactId = eq(sourceRecordId),
            category = eq(0),
            occurredAt = eq(occurredAt)
        )

        // then — DATA EVENT notification is published
        val notificationCaptor = argumentCaptor<Notification<*>>()
        verify(notificationPublisher, times(1))
            .publish(notificationCaptor.capture())

        Assertions.assertEquals(
            "CONTACT_DELETED",
            notificationCaptor.firstValue.eventType
        )

        // then — TELEMETRY is recorded
        verify(telemetryService, times(1))
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

        // contact soft-deleted with ViSOR enabled
        whenever(contactRepository.existsByIdAndVisorContactTrue(sourceRecordId))
            .thenReturn(true)

        whenever(contactRepository.existsByIdAndSoftDeletedFalse(sourceRecordId))
            .thenReturn(false)

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
        val notifications = offenderDeltaService.prepare(delta)

        // then — exactly one notification prepared
        Assertions.assertEquals(1, notifications.size)

        Assertions.assertEquals(
            "CONTACT_DELETED",
            notifications.first().eventType
        )

        // when — NOTIFY
        notifications.forEach(offenderDeltaService::notify)

        // then — DOMAIN EVENT (DELETED) is published
        verify(domainEventService).publishContactDeleted(
            crn = eq("X111111"),
            contactId = eq(sourceRecordId),
            category = eq(4),
            occurredAt = eq(occurredAt)
        )

        // then — DATA EVENT notification is published
        val notificationCaptor = argumentCaptor<Notification<*>>()
        verify(notificationPublisher, times(1))
            .publish(notificationCaptor.capture())

        Assertions.assertEquals(
            "CONTACT_DELETED",
            notificationCaptor.firstValue.eventType
        )

        // then — TELEMETRY is recorded
        verify(telemetryService, times(1))
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
        val notifications = offenderDeltaService.prepare(delta)

        // then — exactly one notification prepared
        Assertions.assertEquals(1, notifications.size)

        Assertions.assertEquals(
            "CONTACT_DELETED",
            notifications.first().eventType
        )

        // when — NOTIFY
        notifications.forEach(offenderDeltaService::notify)

        // then — NO DOMAIN EVENTS published (VISOR flag cannot be resolved)
        verify(domainEventService, never())
            .publishContactUpdated(any(), any(), any(), any())

        verify(domainEventService, never())
            .publishContactDeleted(any(), any(), any(), any())

        // then — DATA EVENT notification is published
        val notificationCaptor = argumentCaptor<Notification<*>>()
        verify(notificationPublisher, times(1))
            .publish(notificationCaptor.capture())

        Assertions.assertEquals(
            "CONTACT_DELETED",
            notificationCaptor.firstValue.eventType
        )

        // then — TELEMETRY is recorded
        verify(telemetryService, times(1))
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