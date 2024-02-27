package uk.gov.justice.digital.hmpps.integration.delius.sentence

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integration.delius.person.Person
import uk.gov.justice.digital.hmpps.integration.delius.provider.entity.Institution
import uk.gov.justice.digital.hmpps.integration.delius.reference.entity.ReferenceData
import java.time.ZonedDateTime

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

    @ManyToOne
    @JoinColumn(name = "institution_id")
    val institution: Institution?,

    @OneToMany(mappedBy = "custody")
    val releases: List<Release> = listOf(),

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "custody_id")
    val id: Long
) {
    fun mostRecentRelease() = releases.maxWithOrNull(compareBy({ it.date }, { it.createdDateTime }))
}

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

    @Id
    @Column(name = "disposal_id")
    val id: Long
)

@Immutable
@Entity
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Event(

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
class Release(

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody,

    @ManyToOne
    @JoinColumn(name = "release_type_id")
    val type: ReferenceData,

    @Column(name = "actual_release_date")
    val date: ZonedDateTime,

    @ManyToOne
    @JoinColumn(name = "institution_id")
    val institution: Institution?,

    @Column(columnDefinition = "clob")
    val notes: String?,

    @OneToOne(mappedBy = "release")
    val recall: Recall?,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Column(name = "created_datetime")
    val createdDateTime: ZonedDateTime,

    @Id
    @Column(name = "release_id")
    val id: Long
)

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0")
class Recall(

    @OneToOne
    @JoinColumn(name = "release_id")
    val release: Release,

    @Column(name = "recall_date")
    val date: ZonedDateTime,

    @ManyToOne
    @JoinColumn(name = "recall_reason_id")
    val reason: RecallReason,

    @Column(columnDefinition = "clob")
    val notes: String?,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "recall_id")
    val id: Long,
)

@Immutable
@Entity
@Table(name = "r_recall_reason")
class RecallReason(

    val code: String,
    val description: String,

    @Id
    @Column(name = "recall_reason_id")
    val id: Long,
)

interface CustodyRepository : JpaRepository<Custody, Long> {
    fun findAllByDisposalEventPersonCrn(crn: String): List<Custody>
}