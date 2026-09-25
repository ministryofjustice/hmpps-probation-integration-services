package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.model.CodedValue

@Entity
@Immutable
@Table(name = "borough")
class ProbationDeliveryUnit(
    @Id
    @Column(name = "borough_id")
    val id: Long,
    val code: String,
    val description: String,
    @ManyToOne
    @JoinColumn("probation_area_id")
    val provider: Provider,
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,
) {
    fun toCodedValue() = CodedValue(code, description)
}