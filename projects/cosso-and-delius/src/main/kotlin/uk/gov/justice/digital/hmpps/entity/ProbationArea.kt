package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "probation_area")
class ProbationArea(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    val code: String,
    val description: String,
)