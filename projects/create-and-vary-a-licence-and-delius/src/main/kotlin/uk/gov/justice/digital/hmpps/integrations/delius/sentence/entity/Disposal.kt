package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person

@Immutable
@Entity
@Where(clause = "active_flag = 1 and soft_deleted = 0")
class Disposal(

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val type: DisposalType,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "disposal_id")
    val id: Long
)

@Immutable
@Entity
@Where(clause = "active_flag = 1 and soft_deleted = 0")
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
@Table(name = "r_disposal_type")
class DisposalType(

    @Column(name = "sentence_type")
    val sentenceType: String,

    @Id
    @Column(name = "disposal_type_id")
    val id: Long
)

interface DisposalRepository : JpaRepository<Disposal, Long> {
    @Query(
        """
        select d from Disposal d
        where d.event.person.crn = :crn
        and d.type.sentenceType in ('NC', 'SC')
    """
    )
    fun findCustodialSentences(crn: String): List<Disposal>
}
