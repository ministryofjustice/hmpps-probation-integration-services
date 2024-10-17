package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

@Entity
@Table(name = "offender_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
@SequenceGenerator(name = "offender_manager_id_seq", sequenceName = "offender_manager_id_seq", allocationSize = 1)
class PersonManager(
    @Id
    @Column(name = "offender_manager_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offender_id_seq")
    val id: Long? = null,

    @Column(name = "allocation_date")
    val allocationDate: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "allocation_reason_id")
    val allocationReason: ReferenceData,

    @Column(name = "end_date")
    val endDate: LocalDate? = null,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,

    @Column(name = "staff_employee_id")
    val staffEmployeeID: Long,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @Column(name = "trust_provider_team_id")
    val trustProviderTeamId: Long,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,
)

@Immutable
@Entity
@Table(name = "staff")
class Staff(
    @Id
    @Column(name = "staff_id")
    val id: Long,

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,
)

@Immutable
@Entity
@Table(name = "team")
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long = 0,

    @Column(columnDefinition = "char(6)")
    val code: String,

    @Column
    val description: String,
)