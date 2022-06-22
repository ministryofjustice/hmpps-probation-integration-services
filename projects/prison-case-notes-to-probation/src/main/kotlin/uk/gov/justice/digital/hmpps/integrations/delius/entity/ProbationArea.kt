package uk.gov.justice.digital.hmpps.integrations.delius.entity

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Immutable
@Entity(name = "probation_area")
class ProbationArea(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @Column(name = "code")
    val code: String,

    @ManyToOne
    @JoinColumn(
        name = "institution_id",
        referencedColumnName = "institution_id",
        updatable = false
    )

    val institution: Institution? = null
)

@Immutable
@Entity(name = "r_institution")
class Institution(
    @Id
    @Column(name = "institution_id")
    val id: Long,

    @Column(name = "nomis_cde_code")
    val nomisCode: String,

    )
