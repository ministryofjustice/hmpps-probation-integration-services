package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "probation_area")
class Provider(
    @Column(name = "code", columnDefinition = "char(3)")
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
    val telephone: String?,
    val emailAddress: String?,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val lau: Lau,

    @Id
    @Column(name = "team_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "district")
class Lau(
    @ManyToOne
    @JoinColumn(name = "borough_id")
    val pdu: Pdu,

    val code: String,
    val description: String,

    @Id
    @Column(name = "district_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "borough")
class Pdu(
    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    val code: String,
    val description: String,

    @Id
    @Column(name = "borough_id")
    val id: Long,
)