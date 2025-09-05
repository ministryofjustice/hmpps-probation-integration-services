package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter

@Entity
@Immutable
@Table(name = "district")
class LocalAdminUnit(
    @Id
    @Column(name = "district_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "borough_id")
    val probationDeliveryUnit: ProbationDeliveryUnit,

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,
)
