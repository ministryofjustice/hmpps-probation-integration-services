package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "offender_manager")
class CommunityManagerEntity(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,
    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: StaffEntity,
    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,
    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false,
    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    val active: Boolean = true,
)
