package uk.gov.justice.digital.hmpps.entity.user

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "probation_area")
class Provider(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(3)")
    val code: String,

    val description: String,

    @Column(name = "end_date")
    val endDate: LocalDate? = null,

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true
)