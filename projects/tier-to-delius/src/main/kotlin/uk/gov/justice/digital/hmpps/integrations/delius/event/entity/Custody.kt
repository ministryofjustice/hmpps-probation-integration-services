package uk.gov.justice.digital.hmpps.integrations.delius.event.entity

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
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @OneToMany(mappedBy = "custody")
    val releases: List<Release>,

    @Column(updatable = false, columnDefinition = "NUMBER")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)