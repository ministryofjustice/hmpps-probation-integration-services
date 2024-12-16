package uk.gov.justice.digital.hmpps.integrations.delius.documents.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class MainOffence(

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event?,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "main_offence_id")
    val id: Long,
)

@Entity
@Immutable
@Table(name = "r_offence")
data class Offence(

    @Column
    val subCategoryDescription: String,

    @Id
    @Column(name = "offence_id")
    val id: Long
)
