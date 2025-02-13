package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@Table(name = "r_breach_notice_type")
class WarningType(
    val code: String,
    val description: String,
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,
    @Id @Column(name = "breach_notice_type_id")
    val id: Long
)

interface WarningTypeRepository : JpaRepository<WarningType, Long> {
    fun findAllBySelectableTrue(): List<WarningType>
}