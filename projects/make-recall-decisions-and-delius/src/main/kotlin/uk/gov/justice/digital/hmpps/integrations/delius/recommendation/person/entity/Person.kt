package uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction

@Immutable
@Entity
@Table(name = "offender")
class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long,
    @Column(columnDefinition = "CHAR(7)")
    val crn: String,
    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,
    @OneToOne(mappedBy = "person")
    val manager: PersonManager? = null,
)

@Immutable
@Entity
@SQLRestriction("active_flag = 1")
@Table(name = "offender_manager")
class PersonManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,
    @OneToOne
    @JoinColumn(name = "offender_id")
    val person: Person,
    @Column(name = "probation_area_id")
    val providerId: Long,
    @Column(name = "team_id")
    val teamId: Long,
    @Column(name = "allocation_staff_id")
    val staffId: Long,
    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,
)
