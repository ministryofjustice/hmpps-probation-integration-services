package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable

@Immutable
@Entity(name = "Provider")
@Table(name = "probation_area")
class Provider(
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
    val institution: Prison? = null
)

@Immutable
@Entity(name = "Prison")
@Table(name = "r_institution")
class Prison(
    @Id
    @Column(name = "institution_id")
    val id: Long,

    @Column(name = "nomis_cde_code")
    val nomisCode: String

)
