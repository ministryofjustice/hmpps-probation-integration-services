package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "offender_manager")
class PersonManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "team_id")
    val teamId: Long,

    @Column(name = "allocation_staff_id")
    val staffId: Long,

    @Column(name = "probation_area_id")
    val probationAreaId: Long,

    @Column(name = "active_flag", columnDefinition = "NUMBER")
    val active: Boolean = true,

    @Column(name = "soft_deleted", columnDefinition = "NUMBER", nullable = false)
    var softDeleted: Boolean = false

)
