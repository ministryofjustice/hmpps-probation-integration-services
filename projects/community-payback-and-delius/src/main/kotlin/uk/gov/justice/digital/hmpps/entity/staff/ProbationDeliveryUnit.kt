package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Immutable
@Table(name = "borough")
class ProbationDeliveryUnit(
    @Id
    @Column(name = "borough_id")
    val id: Long,

    val code: String,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @Column
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true
)

interface ProbationDeliveryUnitRepository : JpaRepository<ProbationDeliveryUnit, Long> {
    fun findByCodeAndProviderCodeAndSelectableIsTrue(code: String, providerCode: String): ProbationDeliveryUnit?
}

