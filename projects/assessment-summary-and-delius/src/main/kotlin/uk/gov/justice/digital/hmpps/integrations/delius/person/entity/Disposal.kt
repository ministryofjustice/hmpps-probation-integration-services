package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable

@Entity
@Immutable
class Disposal(
    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val type: DisposalType,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "disposal_id")
    val id: Long,
)

@Entity
@Immutable
@Table(name = "r_disposal_type")
class DisposalType(
    @Column
    val sentenceType: String,

    @Id
    @Column(name = "disposal_type_id")
    val id: Long,
)
