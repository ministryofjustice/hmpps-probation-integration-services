package uk.gov.justice.digital.hmpps.integrations.delius.event.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.CourtAppearance
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Court
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Officer
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.ProviderEmployee
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0")
@Table(name = "event")
class Event(

    @Column(name = "offender_id", nullable = false)
    val personId: Long,

    @OneToOne(mappedBy = "event", fetch = FetchType.EAGER)
    val mainOffence: MainOffence? = null,

    @Column(name = "in_breach", columnDefinition = "number")
    val inBreach: Boolean,

    @Column(name = "breach_end")
    val breachEnd: LocalDate? = null,

    @Column(name = "conviction_date")
    val convictionDate: LocalDate?,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal? = null,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    val active: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,

    @Column(name = "event_number")
    val eventNumber: String,

    @Column(name = "ftc_count", nullable = false)
    val failureToComplyCount: Long,

    @Column(name = "referral_date", nullable = false)
    val referralDate: LocalDate,

    @OneToMany(mappedBy = "event")
    val additionalOffences: List<AdditionalOffence> = emptyList(),

    @OneToMany(mappedBy = "event")
    val courtAppearances: List<CourtAppearance> = emptyList(),

    @OneToMany(mappedBy = "event")
    val orderManagers: List<OrderManager> = emptyList(),

    @Column(name = "created_by_user_id")
    val createdByUserId: Long? = null,

    @Column(name = "created_datetime")
    val createdDatetime: LocalDateTime? = null,

    @Column(name = "last_updated_user_id")
    val lastUpdatedUserId: Long? = null,

    @Column(name = "last_updated_datetime")
    val lastUpdatedDatetime: LocalDateTime? = null,

    @ManyToOne
    @JoinColumn(name = "court_id")
    val court: Court?,

    )

interface EventRepository : JpaRepository<Event, Long> {

    @EntityGraph(attributePaths = ["mainOffence", "court", "disposal"])
    fun findAllByPersonId(personId: Long): List<Event>

    @EntityGraph(attributePaths = ["mainOffence", "court", "disposal"])
    fun findAllByPersonIdAndActiveIsTrue(personId: Long): List<Event>

    @EntityGraph(attributePaths = ["mainOffence", "court", "disposal"])
    fun findByPersonIdAndId(personId: Long, id: Long): Event?

    @Query(
        """
        select
        case when d.disposal_id is null and ca.outcome_code = '101' then 1 else 0 end as awaitingPsr
         from event e
         left join disposal d on d.event_id = e.event_id and d.soft_deleted = 0
         left join (select ca.event_id, oc.code_value as outcome_code
            from court_appearance ca
            join r_standard_reference_list oc on ca.outcome_id = oc.standard_reference_list_id) ca
            on ca.event_id = e.event_id
        where e.event_id = :eventId
    """,
        nativeQuery = true
    )
    fun awaitingPSR(eventId: Long): Int
}

fun EventRepository.getByPersonAndEventNumber(person: Person, eventId: Long) = findByPersonIdAndId(person.id, eventId)
    ?: throw NotFoundException("Conviction with ID $eventId for Offender with crn ${person.crn} not found")

@Entity
@Immutable
@Table(name = "order_manager")
class OrderManager(

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff?,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val probationArea: ProbationAreaEntity,

    @OneToOne
    @JoinColumn(name = "allocation_team_id")
    val team: Team?,

    val allocationDate: ZonedDateTime,

    val endDate: ZonedDateTime?,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "order_manager_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "allocation_reason_id")
    val allocationReason: ReferenceData? = null,

    @OneToOne
    @JoinColumn(name = "provider_employee_id")
    val providerEmployee: ProviderEmployee? = null,

    @OneToOne
    @JoinColumn(name = "provider_team_id")
    val providerTeam: ProviderTeam? = null,

    @ManyToOne
    @JoinColumn(name = "transfer_reason_id")
    val transferReason: TransferReason? = null,

    @OneToOne
    @JoinColumns(
        JoinColumn(
            name = "staff_employee_id",
            referencedColumnName = "staff_employee_id",
            insertable = false,
            updatable = false
        ),
        JoinColumn(
            name = "trust_provider_flag",
            referencedColumnName = "trust_provider_flag",
            insertable = false,
            updatable = false
        )
    )
    val officer: Officer? = null,

    @JoinColumns(
        JoinColumn(
            name = "trust_provider_team_id",
            referencedColumnName = "trust_provider_team_id",
            insertable = false,
            updatable = false
        ),
        JoinColumn(
            name = "trust_provider_flag",
            referencedColumnName = "trust_provider_flag",
            insertable = false,
            updatable = false
        )
    ) @OneToOne
    val trustProviderTeam: AllTeam? = null,

    )

@Entity
@Immutable
@Table(name = "r_transfer_reason")
class TransferReason(
    @Id
    @Column(name = "transfer_reason_id")
    val id: Long,

    @Column(name = "code")
    val code: String
)

@Entity
@Immutable
class AllTeam(
    @Column(name = "trust_provider_flag")
    val trustProvideFlag: Long,

    @Id
    @Column(name = "trust_provider_team_id")
    val trustProviderTeamId: Long,

    @JoinColumn(name = "probation_area_id")
    @OneToOne
    val probationArea: ProbationAreaEntity? = null,

    @Column(name = "description")
    val description: String? = null,

    @Column(name = "telephone")
    val telephone: String? = null,

    @JoinColumn(name = "district_id")
    @OneToOne
    val district: District? = null
)

