package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Entity
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Event(
    @Id
    @Column(name = "event_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal? = null,

    @OneToOne
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    val mainOffence: MainOffence? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

)

interface EventRepository : JpaRepository<Event, Long> {

    fun findByPersonIdAndDisposalsNotEmptyAndActiveTrueAndSoftDeletedFalse(personId: Long): List<Event>

    @Query(
        """
        SELECT DISTINCT e FROM Event e
        WHERE e.person.id = :personId
        """
    )
    fun findActiveSentencedEventsByPersonId(personId: Long): List<Event>
}