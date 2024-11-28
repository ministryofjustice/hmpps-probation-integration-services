package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter

@Entity
@Immutable
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
data class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(name = "nomsNumber", columnDefinition = "char(7)")
    val prisonerId: String,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)
