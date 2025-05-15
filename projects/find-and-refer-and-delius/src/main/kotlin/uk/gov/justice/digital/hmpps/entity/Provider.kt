package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.Immutable

@Immutable
@Entity(name = "Provider")
@Table(name = "probation_area")
class Provider(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(3)")
    val code: String,

    val description: String,

    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,

    @Column
    @Convert(converter = YesNoConverter::class)
    val establishment: Boolean? = null,
)

