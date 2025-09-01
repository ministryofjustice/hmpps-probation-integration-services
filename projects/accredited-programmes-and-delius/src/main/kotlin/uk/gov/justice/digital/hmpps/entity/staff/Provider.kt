package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.model.CodedValue

@Entity
@Immutable
@Table(name = "probation_area")
class Provider(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,
    @Column(columnDefinition = "char(3)")
    val code: String,
    val description: String,
) {
    fun toCodedValue() = CodedValue(code, description)
}
