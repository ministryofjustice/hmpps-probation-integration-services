package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "event")
@SequenceGenerator(name = "event_id_seq", sequenceName = "event_id_seq", allocationSize = 1)
class Event(
    @Id
    @Column(name = "event_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_id_seq")
    val id: Long? = null,

    @Column(name = "consecutive_to_event_id")
    val consecutiveId: Long? = null,

    @Column(name = "concurrent_with_event_id")
    val concurrentId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @Column(name = "event_number", nullable = false)
    val number: String,

    @Column
    val referralDate: LocalDate,

    @Column(name = "notes", columnDefinition = "clob")
    val notes: String? = null,

    @Column(columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,

    @Column
    val partitionAreaId: Long = 0L,

    @Version
    @Column(name = "row_version")
    var version: Long = 0,

    @CreatedBy
    @Column(name = "created_by_user_id")
    var createdByUserId: Long = 0,

    @CreatedDate
    @Column(name = "created_datetime")
    var createdDateTime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedBy
    @Column(name = "last_updated_user_id")
    var lastModifiedUserId: Long = 0,

    @Column(nullable = false)
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(columnDefinition = "NUMBER")
    val inBreach: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "NUMBER", nullable = false)
    val active: Boolean,

    @Column
    val breachEnd: LocalDate? = null,

    @Column
    val ftcCount: Long,

    @Column(columnDefinition = "NUMBER", nullable = false)
    val pendingTransfer: Boolean = false,

    @Column
    val convictionDate: LocalDate? = null,

    @Column
    val firstReleaseDate: LocalDate? = null,

    @Column(columnDefinition = "NUMBER", nullable = false)
    val pssRqmntFlag: Boolean = false,

    @Column
    val courtId: Long? = null,

    @Column(name = "court_case_urn")
    val courtCaseUrn: String? = null,
)

@Entity
@EntityListeners(AuditingEntityListener::class)
@SequenceGenerator(name = "order_manager_id_seq", sequenceName = "order_manager_id_seq", allocationSize = 1)
@Table(name = "order_manager")
class OrderManager(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_manager_id_seq")
    @Column(name = "order_manager_id")
    val id: Long? = null,

    @Column(name = "allocation_date")
    val allocationDate: LocalDate,

    @Column(name = "allocation_team_id")
    val teamId: Long,

    @Column(name = "allocation_staff_id")
    val staffId: Long,

    @Column(name = "soft_deleted", columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,

    @Column(name = "end_date")
    val endDate: LocalDateTime? = null,

    @Column
    val partitionAreaId: Long = 0,

    @Version
    var rowVersion: Long = 0,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "allocation_reason_id")
    val allocationReason: ReferenceData,

    @Column(name = "provider_employee_id")
    val providerEmployeeId: Long? = null,

    @CreatedDate
    val createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "provider_team_id")
    val providerTeamId: Long? = null,

    @Column
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column(name = "last_updated_user_id")
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @ManyToOne
    @JoinColumn(name = "transfer_reason_id")
    val transferReason: TransferReason,

    @Column(name = "trust_provider_flag", columnDefinition = "number")
    val trustProviderFlag: Boolean = false,

    @Column(name = "staff_employee_id")
    val staffEmployeeId: Long? = null,

    @Column(name = "trust_provider_team_id")
    val trustProviderTeamId: Long? = null,

    @Column(name = "probation_area_id")
    val providerId: Long? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,
)

interface EventRepository : JpaRepository<Event, Long> {
    @Query(
        """
    SELECT CAST(COALESCE(MAX(CAST(e.number AS int)), 0) + 1 AS string) 
    FROM Event e 
    WHERE e.person.id = :personId
    """
    )
    fun getNextEventNumber(personId: Long): String

    @Query(
        """
        select e from Event e 
        where lower(e.notes) like lower(concat('%', :caseUrn, '%'))
        and e.person.crn = :crn
    """
    )
    fun findEventByCaseUrnAndCrn(caseUrn: String?, crn: String?): Event?

    @Query(
        """
        select e from Event e
        where (e.notes IS NULL OR lower(e.notes) not like lower(concat('%', :caseUrn, '%')))
        and e.person.crn = :crn
    """
    )
    fun findActiveEventsExcludingCaseUrn(caseUrn: String?, crn: String?): List<Event>?
}

interface OrderManagerRepository : JpaRepository<OrderManager, Long>

@Entity
@Immutable
@Table(name = "r_transfer_reason")
class TransferReason(
    @Id
    @Column(name = "transfer_reason_id")
    val id: Long,

    @Column(name = "code")
    val code: String
) {
    enum class Reason(val code: String) {
        CASE_ORDER("CASE ORDER"),
    }
}

interface TransferReasonRepository : JpaRepository<TransferReason, Long> {
    fun findByCode(code: String): TransferReason?
}

fun TransferReasonRepository.caseOrderTransferReason() =
    findByCode(
        TransferReason.Reason.CASE_ORDER.code,
    )
        ?: throw NotFoundException(
            "Transfer Reason",
            "code",
            TransferReason.Reason.CASE_ORDER.code
        )
