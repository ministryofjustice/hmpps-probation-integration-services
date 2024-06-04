package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction

@Entity
@Immutable
@Table(name = "offender_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
data class CommunityManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true
)
