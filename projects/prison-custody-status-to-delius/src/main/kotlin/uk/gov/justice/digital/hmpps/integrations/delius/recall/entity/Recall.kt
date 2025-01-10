package uk.gov.justice.digital.hmpps.integrations.delius.recall.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.release.entity.Release
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("soft_deleted = 0")
class Recall(
    @Id
    @SequenceGenerator(name = "recall_id_generator", sequenceName = "recall_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recall_id_generator")
    @Column(name = "recall_id", nullable = false)
    val id: Long = 0,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,

    @Column(name = "recall_date")
    val date: ZonedDateTime,

    @ManyToOne
    @JoinColumn(name = "recall_reason_id", nullable = false)
    val reason: RecallReason,

    @OneToOne
    @JoinColumn(name = "release_id", nullable = false)
    val release: Release,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @Column(nullable = false)
    val partitionAreaId: Long = 0,

    @Column(columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @CreatedBy
    @Column(nullable = false, updatable = false)
    var createdByUserId: Long = 0,

    @LastModifiedBy
    @Column(nullable = false)
    var lastUpdatedUserId: Long = 0,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false)
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()
)

interface RecallRepository : JpaRepository<Recall, Long>
