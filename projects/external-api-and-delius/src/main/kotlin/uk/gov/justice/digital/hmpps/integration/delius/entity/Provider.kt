package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
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

    @Id
    @Column(name = "team_id")
    val id: Long
)