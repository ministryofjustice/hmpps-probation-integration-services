package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class Custody(

    @OneToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @ManyToOne
    @JoinColumn(name = "custodial_status_id")
    val status: ReferenceData,

    @OneToMany(mappedBy = "custody")
    val keyDates: List<KeyDate>,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "custody_id")
    val id: Long
)

@Immutable
@Entity
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Disposal(

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Column(name = "notional_end_date")
    val endDate: LocalDate,

    @Column(name = "entered_notional_end_date")
    val enteredSentenceEndDate: LocalDate? = null,

    @Id
    @Column(name = "disposal_id")
    val id: Long
)

@Immutable
@Entity
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Event(

    @Column(name = "event_number")
    val number: String,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal?,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "event_id")
    val id: Long
)

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0")
class KeyDate(

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody,

    @ManyToOne
    @JoinColumn(name = "key_date_type_id")
    val type: ReferenceData,

    @Column(name = "key_date")
    val date: LocalDate,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "key_date_id")
    val id: Long,
)

interface CustodyRepository : JpaRepository<Custody, Long> {
    @Query(
        """
        select c from Custody c
        join fetch c.status cs
        left join fetch c.keyDates kd
        join fetch c.disposal d
        where d.event.person.crn = :crn
        and cs.code not in ('P', 'T', 'AT')
    """
    )
    fun findCustodialSentences(crn: String): List<Custody>
}
