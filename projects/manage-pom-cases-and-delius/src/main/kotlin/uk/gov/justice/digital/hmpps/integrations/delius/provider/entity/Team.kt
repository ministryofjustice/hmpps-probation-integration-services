package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "team")
class Team(

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    val description: String,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val district: District?,

    @Id
    @Column(name = "team_id")
    val id: Long
)

@Immutable
@Entity
class District(

    @Column(name = "code")
    val code: String,

    val description: String,

    @Id
    @Column(name = "district_id")
    val id: Long
)
