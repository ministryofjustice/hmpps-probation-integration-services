package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
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

    @Column("probation_area_id")
    val regionId: Long,

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,
) {
    fun toCodedValue() = CodedValue(code, description)
}
