package uk.gov.justice.digital.hmpps.service.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class Event(
    @Id
    @Column(name = "event_id", nullable = false)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @OneToOne(mappedBy = "event")
    var disposal: Disposal? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

interface EventRepository : JpaRepository<Event, Long> {

    @Query(
        """
        select count(c) from Event e
        join e.disposal d 
        join d.custody c
        where e.person.crn = :crn
        and e.disposal.custody.status.code in ('A','C','D','R')
        """
    )
    fun countCustodySentences(crn: String): Int
}

fun EventRepository.isInCustody(crn: String) = countCustodySentences(crn) > 0
