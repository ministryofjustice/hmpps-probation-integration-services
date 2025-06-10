package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter

@Entity
@Immutable
class ProbationArea(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @Column(columnDefinition = "char(3)")
    val code: String,

    @Column
    val description: String,

    @OneToMany(mappedBy = "probationArea")
    val boroughs: Set<Borough> = setOf(),

    @Column
    @Convert(converter = YesNoConverter::class)
    val establishment: Boolean? = null,

    @Column
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true
)
