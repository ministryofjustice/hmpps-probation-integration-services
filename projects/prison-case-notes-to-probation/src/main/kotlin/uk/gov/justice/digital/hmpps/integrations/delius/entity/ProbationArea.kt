package uk.gov.justice.digital.hmpps.integrations.delius.entity

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@Immutable
@Entity
class ProbationArea(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(3)")
    val code: String,

    @OneToOne
    @JoinColumn(
        name = "institution_id",
        referencedColumnName = "institution_id",
        updatable = false
    )

    val institution: Institution? = null
)

@Immutable
@Entity
@Table(name = "r_institution")
class Institution(
    @Id
    @Column(name = "institution_id")
    val id: Long,

    @Column(name = "nomis_cde_code")
    val nomisCode: String,

)
