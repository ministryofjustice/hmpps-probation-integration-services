package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Immutable
@Entity
@Table(name = "borough")
class DeliveryUnit(
    @Column(name = "code")
    val code: String,
    val description: String,
    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val region: Provider,
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,
    @Id
    @Column(name = "borough_id")
    val id: Long,
)

interface PduRepository : JpaRepository<DeliveryUnit, Long> {
    @EntityGraph(attributePaths = ["region"])
    @Query("select du from DeliveryUnit du where du.selectable = true")
    fun findAllSelectable(): List<DeliveryUnit>
}
