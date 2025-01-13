package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

@Immutable
@Entity
@Table(name = "event")
class DocEvent(
    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(name = "event_number", nullable = false)
    val eventNumber: String,

    @OneToOne(mappedBy = "event")
    val disposal: DocDisposal? = null,

    @OneToOne(mappedBy = "event")
    val mainOffence: DocMainOffence? = null
)

@Immutable
@Entity
@Table(name = "disposal")
class DocDisposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id", updatable = false)
    val event: DocEvent,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id", updatable = false)
    val type: DocDisposalType,

    @Column(name = "active_flag", updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "r_disposal_type")
class DocDisposalType(
    @Id
    @Column(name = "disposal_type_id")
    val id: Long,
    val description: String
)

@Immutable
@Entity
@Table(name = "r_offence")
class DocOffence(
    @Id
    @Column(name = "offence_id")
    val id: Long,

    val description: String
)

@Immutable
@Entity
@Table(name = "main_offence")
class DocMainOffence(
    @Id
    @Column(name = "main_offence_id")
    val id: Long,

    @JoinColumn(name = "offence_id")
    @OneToOne
    val offence: DocOffence,

    @JoinColumn(name = "event_id")
    @OneToOne
    val event: DocEvent
)
