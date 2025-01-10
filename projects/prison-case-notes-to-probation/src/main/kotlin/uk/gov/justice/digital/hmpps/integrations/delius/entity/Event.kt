package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.Hibernate
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter

@Immutable
@Entity
data class Event(
    @Id
    @Column(name = "event_id")
    val id: Long,

    val offenderId: Long,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal? = null,

    @Column(name = "active_flag", updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    var softDeleted: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Event

        return id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , offenderId = $offenderId , disposal = $disposal , active = $active , softDeleted = $softDeleted )"
    }
}

@Immutable
@Entity
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id", updatable = false)
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id", updatable = false)
    val disposalType: DisposalType,

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
class DisposalType(
    @Id
    @Column(name = "disposal_type_id")
    val id: Long,

    @Column(name = "sentence_type")
    val sentenceType: String
) {
    companion object {
        val CUSTODIAL_CODES = listOf("NC", "SC")
    }
}
