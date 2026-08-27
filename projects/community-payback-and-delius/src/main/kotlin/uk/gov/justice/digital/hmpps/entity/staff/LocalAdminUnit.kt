package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Immutable
@Table(name = "district")
class LocalAdminUnit(
    @Id
    @Column(name = "district_id")
    val id: Long,

    val code: String,

    @ManyToOne
    @JoinColumn(name = "borough_id")
    val probationDeliveryUnit: ProbationDeliveryUnit,

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true
)

interface LocalAdminUnitRepository : JpaRepository<LocalAdminUnit, Long> {
    fun findByCodeAndProbationDeliveryUnitProviderCodeAndSelectableIsTrue(code: String, providerCode: String): LocalAdminUnit?
}

