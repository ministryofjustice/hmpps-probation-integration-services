package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "probation_area")
class Provider(
    @Column(columnDefinition = "char(3)")
    val code: String,

    @Id
    @Column(name = "probation_area_id")
    val id: Long,
)

@Immutable
@Entity
@Table(name = "team")
class Team(

    @Column(columnDefinition = "char(6)")
    val code: String,

    @Id
    @Column(name = "team_id")
    val id: Long,
)

@Immutable
@Entity
@Table(name = "staff")
class Staff(

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    @Id
    @Column(name = "staff_id")
    val id: Long,
)