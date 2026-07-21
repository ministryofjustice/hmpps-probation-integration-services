package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*

@Entity
@Table(name = "probation_area")
class ProbationArea(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,
    @Column(name = "code", columnDefinition = "char(3)")
    val code: String,
    val description: String,
)
