package uk.gov.justice.digital.hmpps.entity.event

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class Custody(
    @Id
    @Column(name = "custody_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "disposal_id", updatable = false)
    val disposal: Disposal,

    @OneToMany(mappedBy = "custody")
    val keyDates: List<KeyDate> = emptyList(),

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)