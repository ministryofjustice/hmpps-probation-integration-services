package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
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
    @Column(name = "offender_id")
    val personId: Long,
    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,
    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,
    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,
)
