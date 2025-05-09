package uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@Table(name = "event")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Event(

    @Column(name = "offender_id")
    val personId: Long,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "event_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "disposal")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Disposal(

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "disposal_id")
    val id: Long
)

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class Custody(

    @OneToOne
    @JoinColumn(name = "disposal_id", updatable = false)
    val disposal: Disposal,

    @Column(columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "custody_id")
    val id: Long
)

interface CustodyRepository : JpaRepository<Custody, Long> {
    fun findAllByDisposalEventPersonId(personId: Long): List<Custody>
}
