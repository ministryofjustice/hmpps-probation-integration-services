package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import uk.gov.justice.digital.hmpps.model.CodedValue
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("end_date is null or end_date > current_date")
class OfficeLocation(
    @Id
    @Column(name = "office_location_id")
    val id: Long,
    @Column(columnDefinition = "char(7)")
    val code: String,
    val description: String,
    val endDate: LocalDate? = null,
) {
    fun toCodedValue() = CodedValue(code, description)
}