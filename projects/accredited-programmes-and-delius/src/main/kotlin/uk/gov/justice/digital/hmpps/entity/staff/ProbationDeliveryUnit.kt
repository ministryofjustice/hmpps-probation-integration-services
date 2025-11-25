package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import uk.gov.justice.digital.hmpps.model.CodedValue

@Entity
@Immutable
@Table(name = "borough")
@SQLRestriction("functional = 'Y'")
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

    val functional: Char
) {
    fun toCodedValue() = CodedValue(code, description)
}
