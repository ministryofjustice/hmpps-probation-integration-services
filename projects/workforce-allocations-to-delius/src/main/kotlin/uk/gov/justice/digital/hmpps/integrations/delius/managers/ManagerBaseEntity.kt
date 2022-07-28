package uk.gov.justice.digital.hmpps.integrations.delius.managers

import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class ManagerBaseEntity : BaseEntity() {
    @JoinColumn(name = "staff_employee_id")
    @ManyToOne
    lateinit var staffEmployee: Staff

    @JoinColumn(name = "allocation_staff_id")
    @ManyToOne
    lateinit var staffAllocation: Staff

    @JoinColumn(name = "allocation_team_id")
    @ManyToOne
    lateinit var team: Team

    @JoinColumn(name = "provider_team_id")
    @ManyToOne
    var teamProvider: Team? = null

    @JoinColumn(name = "trust_provider_team_id")
    @ManyToOne
    var trustProviderTeam: Team? = null

    @Column(name = "trust_provider_flag")
    var trustProviderFlag: Boolean = false

    @Column(name = "allocation_date", nullable = false)
    lateinit var allocationDate: ZonedDateTime

    @Column(name = "end_date")
    var endDate: ZonedDateTime? = null
        set(value) {
            field = value
            active = value == null || value.isAfter(ZonedDateTime.now())
        }

    @JoinColumn(name = "probation_area_id")
    @ManyToOne
    lateinit var provider: Provider

    @Column(name = "partition_area_id", nullable = false)
    var partitionAreaId: Long = 0

    @JoinColumn(name = "allocation_reason_id")
    @ManyToOne
    lateinit var allocationReason: AllocationReason

    @Column(name = "active_flag", columnDefinition = "NUMBER")
    var active: Boolean = true

    fun populate(requestDate: ZonedDateTime, ts: TeamStaffContainer, activeManager: ManagerBaseEntity) {
        team = ts.team
        allocationDate = requestDate
        allocationReason = ts.reason
        staffAllocation = ts.staff
        staffEmployee = ts.staff
        provider = activeManager.provider
        partitionAreaId = activeManager.partitionAreaId
        trustProviderFlag = false
        trustProviderTeam = ts.team
    }
}