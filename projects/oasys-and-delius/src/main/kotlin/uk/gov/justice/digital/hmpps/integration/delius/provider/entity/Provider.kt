package uk.gov.justice.digital.hmpps.integration.delius.provider.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter

@Entity
@Immutable
@Table(name = "probation_area")
class Provider(
    @Column(columnDefinition = "char(3)")
    val code: String,

    val description: String,

    @Id
    @Column(name = "probation_area_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "team")
class Team(

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    val description: String,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @Id
    @Column(name = "team_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "staff")
class Staff(

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    val forename: String,
    val surname: String,

    @Id
    @Column(name = "staff_id")
    val id: Long
)