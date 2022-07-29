package uk.gov.justice.digital.hmpps.integrations.delius.allocations

import uk.gov.justice.digital.hmpps.integrations.delius.provider.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamStaffContainer
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
    lateinit var staff: Staff

    @JoinColumn(name = "allocation_team_id")
    @ManyToOne
    lateinit var team: Team

    @JoinColumn(name = "trust_provider_team_id")
    @ManyToOne
    var trustProviderTeam: Team? = null

    @Column(name = "trust_provider_flag")
    var trustProviderFlag: Boolean = false

    @Column(name = "allocation_date", nullable = false)
    lateinit var startDate: ZonedDateTime

    @Column(name = "active_flag", columnDefinition = "NUMBER")
    var active: Boolean = true

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
    lateinit var allocationReason: ReferenceData

    fun populate(requestDate: ZonedDateTime, ts: TeamStaffContainer, activeManager: ManagerBaseEntity) {
        team = ts.team
        startDate = requestDate
        allocationReason = ts.reason
        staff = ts.staff
        staffEmployee = ts.staff
        provider = activeManager.provider
        partitionAreaId = activeManager.partitionAreaId
        trustProviderFlag = false
        trustProviderTeam = ts.team
    }
}