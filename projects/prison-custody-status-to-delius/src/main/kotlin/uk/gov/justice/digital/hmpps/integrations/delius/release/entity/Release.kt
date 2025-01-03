package uk.gov.justice.digital.hmpps.integrations.delius.release.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.InstitutionId
import uk.gov.justice.digital.hmpps.integrations.delius.recall.entity.Recall
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("soft_deleted = 0")
class Release(
    @Id
    @SequenceGenerator(name = "release_id_generator", sequenceName = "release_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "release_id_generator")
    @Column(name = "release_id", nullable = false)
    val id: Long = 0,

    @Column(name = "row_version", nullable = false)
    @Version
    val version: Long = 0,

    @Column(name = "actual_release_date")
    val date: ZonedDateTime,

    @ManyToOne
    @JoinColumn(name = "release_type_id", nullable = false)
    val type: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "offender_id", nullable = false)
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "custody_id", nullable = false)
    val custody: Custody? = null,

    @Embedded
    val institutionId: InstitutionId? = null,

    @Column(nullable = true)
    val probationAreaId: Long? = null,

    @OneToOne(mappedBy = "release")
    var recall: Recall? = null,

    @Column(nullable = false)
    val partitionAreaId: Long = 0,

    @Column(columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(nullable = false, updatable = false)
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column(nullable = false)
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(nullable = false, updatable = false)
    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(nullable = false)
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()
)

interface ReleaseRepository : JpaRepository<Release, Long>
