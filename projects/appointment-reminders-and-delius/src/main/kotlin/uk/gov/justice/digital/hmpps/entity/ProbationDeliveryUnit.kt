package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "borough")
class ProbationDeliveryUnit(
    @Id
    @Column(name = "borough_id")
    val id: Long,
    val description: String,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: ProbationArea,
)
