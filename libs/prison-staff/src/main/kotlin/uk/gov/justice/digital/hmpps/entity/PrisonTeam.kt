package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity(name = "PrisonTeam")
@Table(name = "team")
class PrisonTeam(

    @Id
    @Column(name = "team_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String

)
