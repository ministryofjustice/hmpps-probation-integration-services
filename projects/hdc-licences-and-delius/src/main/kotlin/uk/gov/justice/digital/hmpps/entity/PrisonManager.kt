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
@Table(name = "prison_offender_manager")
class PrisonManager(
    @Id
    @Column(name = "prison_offender_manager_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: StaffEntity,

    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    val active: Boolean = true
)
